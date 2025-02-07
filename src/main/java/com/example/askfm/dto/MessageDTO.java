package com.example.askfm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    @NotNull(message = "Sender ID cannot be null")
    private Long senderId;
    @NotNull(message = "Recipient ID cannot be null")
    private Long recipientId;
    @NotBlank(message = "Content cannot be empty")
    private String content;
    private LocalDateTime timestamp;
    private boolean read;
}
