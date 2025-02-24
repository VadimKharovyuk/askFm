package com.example.askfm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateDTO {
    @NotBlank
    @Size(max = 1000)
    private String content;

    private MultipartFile media;
    private String tags;

    // Флаг, указывающий, нужно ли удалить существующее медиа
    private boolean removeMedia;
}
