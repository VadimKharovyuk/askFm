package com.example.askfm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "group_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class GroupPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "is_anonymous")
    private boolean isAnonymous;

//    @Column(name = "media", columnDefinition = "bytea")
//    private byte[] media;

    @Column(name = "media_url")
    private String mediaUrl;

    // Добавляем поле для deleteHash
    @Column(name = "media_delete_hash")
    private String mediaDeleteHash;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "group_post_likes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedBy = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupPostComment> comments = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "group_post_mentions")
    private Set<User> mentionedUsers = new HashSet<>();




}
