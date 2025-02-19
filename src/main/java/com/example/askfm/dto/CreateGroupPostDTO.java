package com.example.askfm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupPostDTO {
    @NotBlank(message = "Содержание поста обязательно")
    @Size(max = 1000, message = "Максимальная длина поста - 1000 символов")
    private String content;

    private boolean anonymous;

    private MultipartFile media;
}