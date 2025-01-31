//package com.example.askfm.config;
//
//import com.example.askfm.dto.AdWebSocketDto;
//import com.example.askfm.model.Ad;
//import com.example.askfm.repository.AdRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Random;
//
//@RequiredArgsConstructor
//@Component
//public class AdWebSocketService {
//    private final SimpMessagingTemplate messagingTemplate;
//    private final AdRepository adRepository;
//    private final Random random = new Random();
//    private static final BigDecimal COST_PER_VIEW = new BigDecimal("1.00");
//
//    // 📌 Автоматический показ рекламы каждые 10 секунд
//    @Scheduled(fixedRate = 10000)
//    public void sendRandomAd() {
//        sendAd();
//    }
//
//    // 📌 Метод для ручного вызова показа рекламы
//    public void sendRandomAdManually() {
//        sendAd();
//    }
//
//    @Transactional
//    protected void sendAd() {
//        List<Ad> activeAds = adRepository.findByIsActiveTrue();
//
//        if (activeAds.isEmpty()) {
//            return;
//        }
//
//        Ad ad = activeAds.get(random.nextInt(activeAds.size()));
//
//        if (ad.getRemainingBudget().compareTo(COST_PER_VIEW) >= 0) {
//            ad.setImpressions(ad.getImpressions() + 1);
//            ad.decreaseBudget(COST_PER_VIEW);
//            adRepository.save(ad);
//
//            // Создаем DTO для отправки
//            AdWebSocketDto dto = new AdWebSocketDto();
//            dto.setId(ad.getId());
//            dto.setTitle(ad.getTitle());
//            dto.setContent(ad.getContent());
//            dto.setImageUrl(ad.getImageUrl());
//            dto.setTargetUrl(ad.getTargetUrl());
//
//            messagingTemplate.convertAndSend("/topic/ads", dto);
//        } else {
//            ad.setActive(false);
//            adRepository.save(ad);
//        }
//    }
//}