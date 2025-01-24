package com.example.askfm.model;

import com.example.askfm.enums.TicketStatus;
import com.example.askfm.exception.SpamDetectedException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

import java.time.LocalDateTime;
@Entity
@Data
@Table(name = "support_tickets")
public class SupportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clientName;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 2000)
    @Lob
    private String description;

    @Column(name = "website") // Honeypot поле
    private String website;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Email
    private String email;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (website != null && !website.isEmpty()) {
            throw new SpamDetectedException("Spam detected");
        }
    }
}
