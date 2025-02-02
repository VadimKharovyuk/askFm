package com.example.askfm.service;

import com.example.askfm.dto.AdCreateDto;
import com.example.askfm.dto.AdPublicDto;
import com.example.askfm.dto.AdResponseDto;
import com.example.askfm.maper.AdMapper;
import com.example.askfm.model.Ad;
import com.example.askfm.model.User;
import com.example.askfm.repository.AdLeadRepository;
import com.example.askfm.repository.AdRepository;
import com.example.askfm.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@RequiredArgsConstructor
@Service
public class AdService {
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final AdMapper adMapper;
    private final Random random = new Random();
    private final AdLeadRepository adLeadRepository;

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


    // Новый метод для получения случайной рекламы
    @Transactional
    public Optional<AdPublicDto> getRandomAd() {
        List<Ad> activeAds = adRepository.findByIsActiveTrue();

        if (activeAds.isEmpty()) {
            return Optional.empty();
        }

        Ad ad = activeAds.get(random.nextInt(activeAds.size()));

        if (ad.getRemainingBudget().compareTo(COST_PER_VIEW) >= 0) {
            // Увеличиваем счетчик показов
            ad.setImpressions(ad.getImpressions() + 1);

            // Снимаем монеты за показ
            ad.decreaseBudget(COST_PER_VIEW);
            adRepository.save(ad);

            return Optional.of(adMapper.toPublicDto(ad));
        } else {
            ad.setActive(false);
            adRepository.save(ad);
            return Optional.empty();
        }
    }

    public List<AdResponseDto> getActiveAdsByUser(Long userId) {
        List<Ad> ads = adRepository.findByCreatedByIdAndIsActiveTrue(userId);
        return ads.stream()
                .map(ad -> {
                    AdResponseDto dto = new AdResponseDto();
                    dto.setId(ad.getId());
                    dto.setTitle(ad.getTitle());
                    dto.setContent(ad.getContent());
                    dto.setImpressions(ad.getImpressions());
                    dto.setClickCount(ad.getClickCount());
                    dto.setRemainingBudget(ad.getRemainingBudget());
                    // Добавляем количество лидов
                    dto.setLeadsCount(adLeadRepository.countByAdId(ad.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAd(Long id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Реклама не найдена"));
        adRepository.delete(ad);
    }

    public User addCoins(Long userId, BigDecimal amount, String adminUsername) {
        // Проверка корректности суммы
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Attempt to add non-positive amount of coins: {}", amount);
            throw new IllegalArgumentException("Сумма пополнения должна быть положительной");
        }

        // Находим пользователя или бросаем исключение
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        // Увеличиваем баланс
        user.setBalance(user.getBalance().add(amount));

        // Сохраняем пользователя
        User updatedUser = userRepository.save(user);

        // Логирование
        log.info("Admin {} added {} coins to user {}",
                adminUsername, amount, user.getUsername());

        return updatedUser;
    }
}