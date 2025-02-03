package com.example.askfm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reposts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "original_post_id", nullable = false)
    private Post originalPost;

    @Column(name = "reposted_at", nullable = false)
    private LocalDateTime repostedAt;


}