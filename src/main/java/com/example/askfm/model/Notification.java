package com.example.askfm.model;

import com.example.askfm.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "reposts_id")
    private Repost repost;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator; // Пользователь, который вызвал уведомление

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post; // Связанный пост (для лайков и комментариев)

    @ManyToOne
    @JoinColumn(name = "photo_id") // Добавляем связь с фото
    private Photo photo;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean isRead = false;
}
