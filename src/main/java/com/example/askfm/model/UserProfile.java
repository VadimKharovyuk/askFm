package com.example.askfm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "location")
    private String location;

    @Column(name = "education")
    private String education;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    // Дополнительные поля
    private String website;
    private String occupation;
    private String interests;
}