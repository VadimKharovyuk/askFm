package com.example.askfm.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
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


    @Column(name = "avatar", columnDefinition = "bytea")
    private byte[] avatar;


    @Column(name = "cover", columnDefinition = "bytea")
    private byte[] cover;


    private String bio;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile profile;

    @OneToMany(mappedBy = "author")
    private List<Question> askedQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "recipient")
    private List<Question> receivedQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "subscriber")
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "subscribedTo")
    private List<Subscription> subscribers = new ArrayList<>();
}