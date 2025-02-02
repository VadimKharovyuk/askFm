package com.example.askfm.service;

import com.example.askfm.dto.NewsletterDTO;
import com.example.askfm.enums.NewsletterStatus;
import com.example.askfm.model.Newsletter;
import com.example.askfm.model.NewsletterSubscriber;
import com.example.askfm.model.User;
import com.example.askfm.repository.NewsletterRepository;
import com.example.askfm.repository.NewsletterSubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsletterService {
    private final NewsletterRepository newsletterRepository;
    private final NewsletterSubscriberRepository subscriberRepository;
    private final EmailService emailService;
    private final UserService userService;

//    @PreAuthorize("hasRole('ADMIN')")
    public Newsletter createNewsletter(NewsletterDTO dto, Long adminId) {
        User admin = userService.findById(adminId);

        Newsletter newsletter = Newsletter.builder()
                .subject(dto.getSubject())
                .content(dto.getContent())
                .scheduledTime(dto.getScheduledTime())
                .createdAt(LocalDateTime.now())
                .createdBy(admin)
                .status(dto.isDraft() ? NewsletterStatus.DRAFT : NewsletterStatus.SCHEDULED)
                .build();

        return newsletterRepository.save(newsletter);
    }

    @Scheduled(fixedDelay = 5000)
    public void processScheduledNewsletters() {
        List<Newsletter> scheduledNewsletters = newsletterRepository
                .findByStatusAndScheduledTimeBefore(
                        NewsletterStatus.SCHEDULED,
                        LocalDateTime.now()
                );

        for (Newsletter newsletter : scheduledNewsletters) {
            try {
                sendNewsletterToSubscribers(newsletter);
                newsletter.setStatus(NewsletterStatus.SENT);
                newsletter.setSentAt(LocalDateTime.now());
                newsletterRepository.save(newsletter);

                log.info("Newsletter '{}' sent successfully", newsletter.getSubject());
            } catch (Exception e) {
                log.error("Failed to send newsletter: {}", e.getMessage());
            }
        }
    }

    private void sendNewsletterToSubscribers(Newsletter newsletter) {
        List<NewsletterSubscriber> activeSubscribers = subscriberRepository
                .findByIsActiveTrue();

        for (NewsletterSubscriber subscriber : activeSubscribers) {
            emailService.queueEmail(
                    subscriber.getEmail(),
                    newsletter.getSubject(),
                    newsletter.getContent()
            );
        }
    }

//    @PreAuthorize("hasRole('ADMIN')")
    public List<Newsletter> getAllNewsletters() {
        return newsletterRepository.findAll();
    }

//    @PreAuthorize("hasRole('ADMIN')")
    public void cancelNewsletter(Long newsletterId) {
        Newsletter newsletter = newsletterRepository.findById(newsletterId)
                .orElseThrow(() -> new RuntimeException("Newsletter not found"));

        if (newsletter.getStatus() == NewsletterStatus.SCHEDULED) {
            newsletter.setStatus(NewsletterStatus.CANCELLED);
            newsletterRepository.save(newsletter);
        }
    }


    // Метод для подписки пользователя
    public void subscribeToNewsletter(String email) {
        // Проверяем, не подписан ли уже пользователь
        Optional<NewsletterSubscriber> existingSubscriber = subscriberRepository.findByEmail(email);

        if (existingSubscriber.isPresent()) {
            NewsletterSubscriber subscriber = existingSubscriber.get();
            if (subscriber.isActive()) {
                throw new RuntimeException("Вы уже подписаны на рассылку");
            } else {
                // Если пользователь был отписан, активируем подписку снова
                subscriber.setActive(true);
                subscriberRepository.save(subscriber);
                sendWelcomeBackEmail(email);
            }
        } else {
            // Создаем новую подписку
            NewsletterSubscriber newSubscriber = NewsletterSubscriber.builder()
                    .email(email)
                    .subscribedAt(LocalDateTime.now())
                    .isActive(true)
                    .build();

            subscriberRepository.save(newSubscriber);
            sendWelcomeEmail(email);
        }

        log.info("User {} subscribed to newsletter", email);
    }

    // Метод для отправки приветственного письма
    private void sendWelcomeEmail(String email) {
        String subject = "Добро пожаловать в нашу рассылку!";
        String content = """
            Спасибо за подписку на наши новости!
            
            Вы будете получать самые интересные обновления.
            
            С уважением,
            Команда AskFM
            """;

        emailService.queueEmail(email, subject, content);
    }

    // Метод для отправки письма при повторной подписке
    private void sendWelcomeBackEmail(String email) {
        String subject = "С возвращением!";
        String content = """
            Рады видеть вас снова!
            
            Ваша подписка на рассылку успешно возобновлена.
            
            С уважением,
            Команда AskFM
            """;

        emailService.queueEmail(email, subject, content);
    }
}