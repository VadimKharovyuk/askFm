package com.example.askfm.dto;

import com.example.askfm.enums.GroupCategory;
import com.example.askfm.enums.GroupRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMembershipDTO {
    // Основная информация о группе
    private Long id;
    private String name;
    private String description;
    private boolean isPrivate;
    private GroupCategory category;
    private String avatarBase64;
    private String coverBase64;
    private String rules;
    private Integer membersCount;
    private Integer postsCount;
    private LocalDateTime createdAt;

    // Информация о владельце
    private GroupMemberViewDTO owner;

    // Информация о членстве текущего пользователя
    private GroupRole userRole;
    private LocalDateTime joinedAt;
    private boolean canCreatePosts;
    private boolean canModerate;
    private boolean canInviteMembers;
}
