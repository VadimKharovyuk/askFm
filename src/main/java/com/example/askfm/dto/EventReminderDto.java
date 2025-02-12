package com.example.askfm.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventReminderDto {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long userId;
    private String username;
    private LocalDateTime sentAt;
    private boolean sent;
}
