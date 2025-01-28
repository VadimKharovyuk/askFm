package com.example.askfm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLockDTO {
    @NotBlank(message = "Причина блокировки обязательна")
    private String reason;
}