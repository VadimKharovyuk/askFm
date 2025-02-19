package com.example.askfm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupPostCommentDTO {
    private Long id;
    private Long postId;
    private String content;
    private LocalDateTime createdAt;
    private GroupPostUserDTO author;
}
