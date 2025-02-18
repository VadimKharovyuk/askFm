package com.example.askfm.dto;

import com.example.askfm.enums.GroupCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupDTO {
    @NotBlank(message = "Название группы обязательно")
    @Size(min = 3, max = 100, message = "Название группы должно быть от 3 до 100 символов")
    private String name;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;

    @NotNull(message = "Необходимо указать тип группы (открытая/закрытая)")
    private Boolean isPrivate;

    @NotNull(message = "Категория группы обязательна")
    private GroupCategory category;

    private byte[] cover;
    private byte[] avatar;

    @Size(max = 1000, message = "Правила не должны превышать 1000 символов")
    private String rules;
}
