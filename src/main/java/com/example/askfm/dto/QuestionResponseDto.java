package com.example.askfm.dto;

import com.example.askfm.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponseDto {
    private Long id;
    private String content;
    private User author;           // Для доступа к author.username
    private String authorUsername; // Для случаев, когда вопрос анонимный
    private User recipient;
    private String recipientUsername;
    private boolean anonymous;
    private LocalDateTime createdAt;
    private boolean answered;
    private AnswerResponseDto answer;
    private Integer likes;         // Добавляем поле для лайков
}