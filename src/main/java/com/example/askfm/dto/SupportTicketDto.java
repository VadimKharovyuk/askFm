package com.example.askfm.dto;

import com.example.askfm.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupportTicketDto {
    private Long id;

    private String clientName;
    private String subject;
    private String description;
    private String email;
    private String website; // Honeypot
    private TicketStatus status;
}
