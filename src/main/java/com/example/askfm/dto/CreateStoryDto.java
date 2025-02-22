package com.example.askfm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateStoryDto {
    private String username;
    private byte[] imageData;
    private String deleteHash;
}