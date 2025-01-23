package com.example.askfm.dto;

import lombok.Data;

@Data
public class AnswerRequestDto {
    private String content;
    private Long questionId;
}
