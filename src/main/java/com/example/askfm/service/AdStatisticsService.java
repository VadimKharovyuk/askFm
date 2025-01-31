package com.example.askfm.service;

import com.example.askfm.model.Ad;
import com.example.askfm.repository.AdLeadRepository;
import com.example.askfm.repository.AdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdStatisticsService {
    private final AdRepository adRepository;

    public Map<String, Object> getAdvertiserStatistics(Long userId) {
        Map<String, Object> statistics = new HashMap<>();

        // Получаем все рекламы пользователя
        List<Ad> userAds = adRepository.findByCreatedById(userId);

        // Общее количество показов всех реклам
        int totalImpressions = userAds.stream()
                .mapToInt(Ad::getImpressions)
                .sum();

        // Общее количество кликов
        int totalClicks = userAds.stream()
                .mapToInt(Ad::getClickCount)
                .sum();

        // Количество активных реклам
        long activeAdsCount = userAds.stream()
                .filter(Ad::isActive)
                .count();

        // Общий потраченный бюджет
        BigDecimal totalSpentBudget = userAds.stream()
                .map(ad -> ad.getTotalBudget().subtract(ad.getRemainingBudget()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        statistics.put("totalAds", userAds.size());
        statistics.put("activeAds", activeAdsCount);
        statistics.put("totalImpressions", totalImpressions);
        statistics.put("totalClicks", totalClicks);
        statistics.put("totalSpentBudget", totalSpentBudget);
        statistics.put("averageCTR", calculateAverageCTR(userAds));

        return statistics;
    }

    private BigDecimal calculateAverageCTR(List<Ad> ads) {
        if (ads.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalCTR = ads.stream()
                .map(ad -> {
                    if (ad.getImpressions() == 0) {
                        return BigDecimal.ZERO;
                    }
                    return BigDecimal.valueOf(ad.getClickCount())
                            .divide(BigDecimal.valueOf(ad.getImpressions()), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalCTR.divide(BigDecimal.valueOf(ads.size()), 2, RoundingMode.HALF_UP);
    }

}
