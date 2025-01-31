package com.example.askfm.service;

import com.example.askfm.dto.AdCreateDto;
import com.example.askfm.dto.AdPublicDto;
import com.example.askfm.dto.AdResponseDto;
import com.example.askfm.maper.AdMapper;
import com.example.askfm.model.Ad;
import com.example.askfm.model.User;
import com.example.askfm.repository.AdRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AdService {
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final AdMapper adMapper;
    private final Random random = new Random();

    private static final BigDecimal COST_PER_VIEW = new BigDecimal("1.00");

    @Transactional
    public AdResponseDto createAd(Long userId, AdCreateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (user.getBalance().compareTo(dto.getTotalBudget()) < 0) {
            throw new IllegalArgumentException("Недостаточно монет для создания рекламы");
        }

        // Вычитаем баланс
        user.setBalance(user.getBalance().subtract(dto.getTotalBudget()));
        userRepository.save(user);

        // Создаём рекламу
        Ad ad = adMapper.toEntity(dto);
        ad.setCreatedBy(user);
        ad.setRemainingBudget(dto.getTotalBudget());
        adRepository.save(ad);

        return adMapper.toDto(ad);
    }

    public List<AdResponseDto> getActiveAds() {
        return adRepository.findByIsActiveTrue()
                .stream()
                .map(adMapper::toDto)
                .collect(Collectors.toList());
    }



    public Ad findById(Long adId) {
       return adRepository.findById(adId).orElseThrow();
    }

    public AdPublicDto getPublicAd(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Реклама не найдена"));

        // Проверяем, активна ли реклама
        if (!ad.isActive()) {
            throw new RuntimeException("Реклама неактивна");
        }

        // Проверяем, не закончился ли бюджет
        if (ad.getRemainingBudget().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Бюджет рекламы исчерпан");
        }

        return adMapper.toPublicDto(ad);
    }
    public Map<String, Object> getAdStatistics(Long adId) {
        Ad ad = findById(adId);
        Map<String, Object> statistics = new HashMap<>();

        statistics.put("impressions", ad.getImpressions());
        statistics.put("clicks", ad.getClickCount());
        statistics.put("ctr", calculateCTR(ad));
        statistics.put("remainingBudget", ad.getRemainingBudget());

        return statistics;
    }

    private double calculateCTR(Ad ad) {
        if (ad.getImpressions() == 0) return 0.0;
        return (double) ad.getClickCount() / ad.getImpressions() * 100;
    }


    // Новый метод для получения случайной рекламы
    @Transactional
    public AdPublicDto getRandomAd() {
        List<Ad> activeAds = adRepository.findByIsActiveTrue();

        if (activeAds.isEmpty()) {
            throw new RuntimeException("Нет активных реклам");
        }

        Ad ad = activeAds.get(random.nextInt(activeAds.size()));

        if (ad.getRemainingBudget().compareTo(COST_PER_VIEW) >= 0) {
            // Увеличиваем счетчик показов
            ad.setImpressions(ad.getImpressions() + 1);

            // Снимаем монеты за показ
            ad.decreaseBudget(COST_PER_VIEW);
            adRepository.save(ad);

            return adMapper.toPublicDto(ad);
        } else {
            ad.setActive(false);
            adRepository.save(ad);
            throw new RuntimeException("Бюджет рекламы исчерпан");
        }
    }
    public List<AdResponseDto> getActiveAdsByUser(Long userId) {
        return adRepository.findByCreatedByIdAndIsActiveTrue(userId)
                .stream()
                .map(adMapper::toDto)
                .collect(Collectors.toList());
    }
}