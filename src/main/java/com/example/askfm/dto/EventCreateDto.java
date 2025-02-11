package com.example.askfm.dto;

import com.example.askfm.enums.EventCategory;
import com.example.askfm.valid.ValidFile;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCreateDto {
    @NotBlank(message = "Назва події обов'язкова")
    private String title;

    private String description;


    @ValidFile(
            allowedContentTypes = {"image/jpeg", "image/png"},
            message = "Файл повинен бути зображенням (JPEG або PNG) розміром до 3MB"
    )
    private MultipartFile photo;

    @NotBlank(message = "Локація обов'язкова")
    private String location;

    @NotBlank(message = "Адреса обов'язкова")
    private String address;

    @NotBlank(message = "Місто обов'язкове")
    private String city;

    @NotNull(message = "Категорія обов'язкова")
    private EventCategory category;

    @PositiveOrZero(message = "Ціна повинна бути додатньою або нуль")
    private BigDecimal price;

    @Min(value = 0, message = "Вікове обмеження повинно бути додатнім")
    private Integer ageRestriction;

    private String duration;

    @NotNull(message = "Дата події обов'язкова")
    @Future(message = "Дата події повинна бути в майбутньому")
    private LocalDateTime eventDate;
}
