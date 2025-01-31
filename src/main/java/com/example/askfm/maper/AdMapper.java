package com.example.askfm.maper;

import com.example.askfm.dto.AdCreateDto;
import com.example.askfm.dto.AdPublicDto;
import com.example.askfm.dto.AdResponseDto;
import com.example.askfm.model.Ad;
import org.springframework.stereotype.Component;

@Component
public class AdMapper {

    public AdResponseDto toDto(Ad ad) {
        AdResponseDto dto = new AdResponseDto();
        dto.setId(ad.getId());
        dto.setTitle(ad.getTitle());
        dto.setContent(ad.getContent());
        dto.setImageUrl(ad.getImageUrl());
        dto.setTargetUrl(ad.getTargetUrl());
        dto.setStartTime(ad.getStartTime());
        dto.setEndTime(ad.getEndTime());
        dto.setImpressions(ad.getImpressions());
        dto.setClickCount(ad.getClickCount());
        dto.setTotalBudget(ad.getTotalBudget());
        dto.setRemainingBudget(ad.getRemainingBudget());
        return dto;
    }

    public Ad toEntity(AdCreateDto dto) {
        Ad ad = new Ad();
        ad.setTitle(dto.getTitle());
        ad.setContent(dto.getContent());
        ad.setImageUrl(dto.getImageUrl());
        ad.setTargetUrl(dto.getTargetUrl());
        ad.setStartTime(dto.getStartTime());
        ad.setEndTime(dto.getEndTime());
        ad.setTotalBudget(dto.getTotalBudget());
        ad.setRemainingBudget(dto.getTotalBudget());
        ad.setActive(true);
        return ad;
    }
    public AdPublicDto toPublicDto(Ad ad) {
        AdPublicDto dto = new AdPublicDto();
        dto.setId(ad.getId());
        dto.setTitle(ad.getTitle());
        dto.setContent(ad.getContent());
        dto.setImageUrl(ad.getImageUrl());
        dto.setTargetUrl(ad.getTargetUrl());
        return dto;
    }
}