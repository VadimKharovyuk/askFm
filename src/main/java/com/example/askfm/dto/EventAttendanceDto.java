package com.example.askfm.dto;

import com.example.askfm.enums.ParticipationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 4. DTO для списка участников
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventAttendanceDto {
    private Long id;
    private UserShortDto user;
    private ParticipationType status;
    private LocalDateTime registeredAt;
}
