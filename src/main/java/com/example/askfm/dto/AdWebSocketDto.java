package com.example.askfm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdWebSocketDto {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private String targetUrl;

}
