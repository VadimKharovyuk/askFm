package com.example.askfm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDTO {
    @NotBlank
    @Size(min = 1, max = 20)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 1, max = 40)
    private String password;
}