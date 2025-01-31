package com.example.askfm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AdLeadFormDTO {

    // Поле для полного имени пользователя
    @NotBlank(message = "Полное имя не может быть пустым")
    private String username;

    // Поле для email, проверка на корректность email
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;
}