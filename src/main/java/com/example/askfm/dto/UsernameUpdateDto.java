package com.example.askfm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsernameUpdateDto {
    @NotBlank(message = "Новое имя пользователя не может быть пустым")
    @Size(min = 3, max = 20, message = "Имя пользователя должно быть от 3 до 20 символов")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Имя пользователя может содержать только буквы, цифры и знак подчеркивания")
    private String newUsername;
}