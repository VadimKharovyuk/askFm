package com.example.askfm.model;

import com.example.askfm.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO; // Баланс монет

    @Column
    private LocalDate lastRewardDate;//награда рандом

    private LocalDate lastPageVisitDate; //награда за переход

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role;

    @Column(name = "avatar", columnDefinition = "bytea")
    private byte[] avatar;


    @Column(name = "cover", columnDefinition = "bytea")
    private byte[] cover;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Column(name = "is_locked", columnDefinition = "boolean default false")
    private boolean locked = false;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "lock_reason")
    private String lockReason;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    private UserProfile profile;

    @OneToMany(mappedBy = "author")
    private List<Question> askedQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "recipient")
    private List<Question> receivedQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "subscriber")
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "subscribedTo")
    private List<Subscription> subscribers = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<SavedPost> savedPosts = new HashSet<>();

    // Добавленные атрибуты для отслеживания визитов
    @OneToMany(mappedBy = "visitedUser")
    @OrderBy("visitedAt DESC")  // Явно указываем порядок
    private List<Visit> pageVisits = new ArrayList<>();

    @OneToMany(mappedBy = "visitor")
    @OrderBy("visitedAt DESC")  // Явно указываем порядок
    private List<Visit> myVisits = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Repost> reposts = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_blocks",
            joinColumns = @JoinColumn(name = "blocker_id"),
            inverseJoinColumns = @JoinColumn(name = "blocked_id")
    )
    private Set<User> blockedUsers = new HashSet<>();

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "creator")
    private List<Event> createdEvents;


    @OneToMany(mappedBy = "user")
    private List<EventAttendance> eventAttendances = new ArrayList<>();

}