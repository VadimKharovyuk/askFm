package com.example.askfm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ads")
public class Ad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private String imageUrl;
    private String targetUrl;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int impressions = 0; // Количество показов
    private int clickCount = 0;  // Количество кликов

    private boolean isActive = true; // Активна ли реклама

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalBudget; // Общий бюджет рекламы в монетах

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal remainingBudget; // Оставшийся бюджет

    public void decreaseBudget(BigDecimal amount) {
        if (remainingBudget.compareTo(amount) >= 0) {
            remainingBudget = remainingBudget.subtract(amount);
            if (remainingBudget.compareTo(BigDecimal.ZERO) == 0) {
                isActive = false; // Отключаем рекламу, если бюджет закончился
            }
        }
    }
}