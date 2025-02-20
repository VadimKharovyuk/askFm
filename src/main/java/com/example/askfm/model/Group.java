package com.example.askfm.model;

import com.example.askfm.enums.GroupCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Entity
@Table(name = "groups", indexes = {
        @Index(name = "idx_groups_category", columnList = "category"),
        @Index(name = "idx_groups_pagination", columnList = "created_at DESC, category"),
        @Index(name = "idx_groups_name", columnList = "name"),
        @Index(name = "idx_groups_name_category", columnList = "name, category"),
        @Index(name = "idx_groups_private", columnList = "is_private")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(columnDefinition = "TEXT")

    private String description;

    @Column(name = "is_private")
    private boolean isPrivate;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private GroupCategory category;

    @Column(name = "cover", columnDefinition = "bytea")
    private byte[] cover;
    @Column(name = "avatar", columnDefinition = "bytea")
    private byte[] avatar;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "members_count")
    @Builder.Default
    private Integer membersCount = 0;
    @Column(name = "posts_count")
    @Builder.Default
    private Integer postsCount = 0;

    @Column(name = "rules", columnDefinition = "TEXT")
    private String rules;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<GroupMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<GroupJoinRequest> joinRequests = new ArrayList<>();

}
