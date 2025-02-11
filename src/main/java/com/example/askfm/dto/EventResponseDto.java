package com.example.askfm.dto;

import com.example.askfm.enums.EventCategory;
import com.example.askfm.enums.ParticipationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponseDto {
    private Long id;
    private UserShortDto creator;
    private String title;
    private String description;
    private String photoBase64;
    private String location;
    private String address;
    private String city;
    private EventCategory category;
    private LocalDateTime eventDate;
    private LocalDateTime createdAt;
    private BigDecimal price;
    private Integer ageRestriction;
    private String duration;
    private int goingCount;
    private int interestedCount;
    private ParticipationType userParticipationType;
}
