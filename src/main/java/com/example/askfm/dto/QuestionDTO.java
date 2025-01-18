package com.example.askfm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    @NotBlank
    private String content;

    @NotBlank
    private String recipientUsername;

    private boolean anonymous;
}
