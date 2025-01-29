package com.example.askfm.model;

import com.example.askfm.enums.ReportCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false  )
    private Post post;

    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportCategory category;

    @Column(nullable = false)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Счетчик похожих жалоб
    @Column(name = "similar_reports_count")
    private Integer similarReportsCount = 0;




}