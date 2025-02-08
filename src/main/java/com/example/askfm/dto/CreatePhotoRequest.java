package com.example.askfm.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePhotoRequest {
    private BigDecimal price;
    private String description;
    private Boolean isNSFW;
}