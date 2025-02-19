package com.example.askfm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMembersListDTO {
    private Long groupId;
    private String groupName;
    private String groupAvatarBase64;
    private boolean isPrivate;
    private int totalMembers;
    private Page<GroupMemberDTO> members;
}