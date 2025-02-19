package com.example.askfm.dto;

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
public class GroupMemberDTO {
    private Long userId;
    private String username;
    private String avatarBase64;
    private GroupRole role;
    private LocalDateTime joinedAt;
}
