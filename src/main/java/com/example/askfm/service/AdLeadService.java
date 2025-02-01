package com.example.askfm.service;

import com.example.askfm.dto.AdLeadFormDTO;
import com.example.askfm.dto.AdLeadResponseDTO;
import com.example.askfm.maper.AdLeadMapper;
import com.example.askfm.model.Ad;
import com.example.askfm.model.AdLead;
import com.example.askfm.model.User;
import com.example.askfm.repository.AdLeadRepository;
import com.example.askfm.repository.AdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class AdLeadService {
    private final AdLeadRepository adLeadRepository;
    private final AdService adService;
    private final UserService userService;
    private final AdRepository adRepository;
    private final AdLeadMapper adLeadMapper;

    private static final BigDecimal COST_PER_CLICK = new BigDecimal("2.00"); // Стоимость за клик
    private static final int PAGE_SIZE = 20; // Размер страницы

    @Transactional
    public AdLead submitLead(Long adId, String username, AdLeadFormDTO leadFormDTO) {
        Ad ad = adService.findById(adId);
        User user = userService.findByUsername(username);

        // Увеличиваем счетчик кликов
        ad.setClickCount(ad.getClickCount() + 1);

        // Снимаем монеты за клик
        ad.decreaseBudget(COST_PER_CLICK);
        adRepository.save(ad);

        AdLead lead = new AdLead();
        lead.setAd(ad);
        lead.setUser(user);
        lead.setUsername(leadFormDTO.getUsername());
        lead.setEmail(leadFormDTO.getEmail());

        return adLeadRepository.save(lead);
    }



    public Page<AdLeadResponseDTO> getAdLeadsByAdId(Long adId, int page) {
        PageRequest pageable = PageRequest.of(page, 20, Sort.by("submittedAt").descending());

        Page<AdLead> leadsPage = adLeadRepository.findByAdId(adId, pageable);
        return leadsPage.map(adLeadMapper::toDto);
    }
}