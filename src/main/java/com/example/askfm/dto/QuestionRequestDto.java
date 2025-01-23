package com.example.askfm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequestDto {
    private String content;
    private Long recipientId;
    private boolean anonymous;
}