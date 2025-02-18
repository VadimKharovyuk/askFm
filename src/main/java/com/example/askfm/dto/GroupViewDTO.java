package com.example.askfm.dto;

import com.example.askfm.enums.GroupCategory;
import com.example.askfm.enums.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupViewDTO {
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


    private MembershipStatus membershipStatus;
    private boolean canJoin;
    private boolean canApproveMembers;
    private LocalDateTime membershipRequestDate;

    // Последние участники (например, 5)
    private List<GroupMemberViewDTO> recentMembers;
}
