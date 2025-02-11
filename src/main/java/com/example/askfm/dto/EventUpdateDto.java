package com.example.askfm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventUpdateDto {
    @NotBlank(message = "Назва події обов'язкова")
    private String title;

    private String description;

    private LocalDateTime eventDate;

    private String city;

    private String address;
}