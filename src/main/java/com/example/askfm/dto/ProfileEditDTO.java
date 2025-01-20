package com.example.askfm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileEditDTO {
    private String bio;
    private LocalDate dateOfBirth;
    private String location;
    private String education;
    private String website;
    private String occupation;
    private String interests;
}