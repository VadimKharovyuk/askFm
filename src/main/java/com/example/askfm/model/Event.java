package com.example.askfm.model;

import com.example.askfm.enums.EventCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EventAttendance> attendances = new ArrayList<>();


    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "photo", columnDefinition = "bytea")
    private byte[] photo;

    @Column
    private String location;

    @Column
    private String address;

    @Column
    private String city;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventCategory category;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column
    private Integer ageRestriction;

    @Column
    private String duration;




}