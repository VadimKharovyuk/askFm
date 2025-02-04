package com.example.askfm.dto;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepostDTO {
    private Long id;
    private Long userId;
    private Long originalPostId;
    private LocalDateTime repostedAt;
    private String username; // Кто сделал репост
//    private String comment; // Опциональный комментарий к репосту
    private LocalDateTime originalPostedAt; // Дата оригинального поста
    private PostDTO originalPost; // Оригинальный пост
    private String authorUsername; // Автор оригинального поста
}