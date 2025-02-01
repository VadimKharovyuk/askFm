package com.example.askfm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 2. DTO для запроса восстановления пароля
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestDTO {
    private String email;
}