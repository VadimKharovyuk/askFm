package com.example.askfm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventListDto {
    private Long id;
    private String title;
    private String photoBase64;
    private LocalDateTime eventDate;
    private String location;
    private String address;
    private String city;
    private int goingCount;
    private int interestedCount;
}
