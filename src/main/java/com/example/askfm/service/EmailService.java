package com.example.askfm.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private final Queue<EmailMessage> emailQueue = new ConcurrentLinkedQueue<>();

    public void queueEmail(String to, String subject, String text) {
        emailQueue.offer(new EmailMessage(to, subject, text));
    }

    @Scheduled(fixedDelay = 5000)
    public void processEmailQueue() {
        EmailMessage email = emailQueue.poll();
        if (email != null) {
            try {
                sendEmail(email.getTo(), email.getSubject(), email.getText());
                log.info("Email sent successfully to: {}", email.getTo());
            } catch (Exception e) {
                log.error("Failed to send email: {}", e.getMessage());
                emailQueue.offer(email);
            }
        }
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
    @Data
    @AllArgsConstructor
    public class EmailMessage {
        private String to;
        private String subject;
        private String text;
    }
}