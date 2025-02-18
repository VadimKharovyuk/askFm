package com.example.askfm.model;

import com.example.askfm.enums.GroupRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
    @ManyToOne
    @JoinColumn(name = "user_id")

    private User user;
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private GroupRole role;
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
}
