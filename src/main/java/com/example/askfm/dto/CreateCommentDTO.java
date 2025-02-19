package com.example.askfm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDTO {
    @NotBlank(message = "Комментарий не может быть пустым")
    @Size(max = 500, message = "Комментарий не может быть длиннее 500 символов")
    private String content;
}