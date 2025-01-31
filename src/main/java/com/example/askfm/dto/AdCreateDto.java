package com.example.askfm.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AdCreateDto {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String imageUrl;
    private String targetUrl;

    @NotNull
    @Future(message = "Дата начала должна быть в будущем")
    private LocalDateTime startTime;

    @NotNull
    @Future(message = "Дата окончания должна быть в будущем")
    private LocalDateTime endTime;

    @NotNull
    @Min(value = 1, message = "Минимальный бюджет — 1 монета")
    private BigDecimal totalBudget;
}