package com.example.askfm.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsletterDTO {
    @NotBlank(message = "Тема не может быть пустой")
    private String subject;

    @NotBlank(message = "Содержание не может быть пустым")
    private String content;

    @NotNull(message = "Укажите время отправки")
    @Future(message = "Время отправки должно быть в будущем")
    private LocalDateTime scheduledTime;

    private boolean isDraft;
}