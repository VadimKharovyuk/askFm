package com.example.askfm.dto;

import com.example.askfm.enums.ReportCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReportCreateDTO {
    @NotNull(message = "ID поста обязателен")
    private Long postId;

    @NotNull(message = "Категория обязательна")
    private ReportCategory category;

    @NotBlank(message = "Причина обязательна")
    @Size(min = 10, max = 500, message = "Причина должна содержать от 10 до 500 символов")
    private String reason;
}
