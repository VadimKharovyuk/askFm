package com.example.askfm.model;

import com.example.askfm.enums.ArticleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "article_type")
    @Enumerated(EnumType.STRING)
    private ArticleType type;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;


    @Column(name = "version_number")
    private Integer versionNumber;

    @PrePersist
    @PreUpdate
    private void prePersist() {
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
        if (versionNumber == null) {
            versionNumber = 1;
        }
    }
}

