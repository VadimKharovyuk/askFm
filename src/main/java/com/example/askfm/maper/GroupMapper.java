package com.example.askfm.maper;

import com.example.askfm.dto.*;
import com.example.askfm.enums.GroupRole;
import com.example.askfm.enums.MembershipStatus;
import com.example.askfm.model.Group;
import com.example.askfm.model.GroupMember;
import com.example.askfm.model.User;
import com.example.askfm.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


@Component
@RequiredArgsConstructor
public class GroupMapper {
    private final ImageService imageService;

    private static final int COVER_WIDTH = 1024;  // Увеличили размер
    private static final int AVATAR_WIDTH = 1024; // Увеличили размер

    public Group toEntity(CreateGroupDTO dto, User owner) throws IOException {
        LocalDateTime now = LocalDateTime.now();

        // Обработка изображений
        byte[] processedCover = null;
        byte[] processedAvatar = null;

        if (dto.getCover() != null) {
            processedCover = imageService.resizeImage(dto.getCover(), COVER_WIDTH);
        }

        if (dto.getAvatar() != null) {
            processedAvatar = imageService.resizeImage(dto.getAvatar(), AVATAR_WIDTH);
        }

        // Создаем группу
        Group group = Group.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .isPrivate(dto.getIsPrivate())
                .category(dto.getCategory())
                .cover(processedCover)
                .avatar(processedAvatar)
                .rules(dto.getRules())
                .createdAt(now)
                .updatedAt(now)
                .membersCount(1)
                .postsCount(0)
                .members(new ArrayList<>())
                .build();

        // Создаем запись о владельце группы
        GroupMember ownerMember = new GroupMember();
        ownerMember.setGroup(group);
        ownerMember.setUser(owner);
        ownerMember.setRole(GroupRole.OWNER);
        ownerMember.setJoinedAt(now);

        group.getMembers().add(ownerMember);

        return group;
    }

    public GroupListDTO toListDto(Group group) {
        return GroupListDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .isPrivate(group.isPrivate())
                .membersCount(group.getMembersCount())
                .postsCount(group.getPostsCount())
                .createdAt(group.getCreatedAt())
                .avatarBase64(group.getAvatar() != null ?
                        imageService.getBase64Avatar(group.getAvatar()) : null)
                .build();
    }

    public GroupViewDTO toViewDto(Group group, MembershipStatus status, LocalDateTime requestDate, GroupRole userRole) {
        GroupMember ownerMember = group.getMembers().stream()
                .filter(member -> member.getRole() == GroupRole.OWNER)
                .findFirst()
                .orElse(null);

        List<GroupMemberViewDTO> recentMembers = group.getMembers().stream()
                .filter(member -> member.getRole() != GroupRole.OWNER)
                .sorted((m1, m2) -> m2.getJoinedAt().compareTo(m1.getJoinedAt()))
                .limit(5)
                .map(this::toMemberViewDto)
                .toList();


        boolean canJoin = status == MembershipStatus.NOT_MEMBER;

        // Обновленная проверка прав на одобрение заявок
        boolean canApproveMembers = userRole != null && userRole.canApproveMembers();

        return GroupViewDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .isPrivate(group.isPrivate())
                .category(group.getCategory())
                .avatarBase64(group.getAvatar() != null ?
                        imageService.getBase64Avatar(group.getAvatar()) : null)
                .coverBase64(group.getCover() != null ?
                        imageService.getBase64Avatar(group.getCover()) : null)
                .rules(group.getRules())
                .membersCount(group.getMembersCount())
                .postsCount(group.getPostsCount())
                .createdAt(group.getCreatedAt())
                .owner(ownerMember != null ? toMemberViewDto(ownerMember) : null)
                .recentMembers(recentMembers)
                .membershipStatus(status)
                .canJoin(canJoin)
                .canApproveMembers(canApproveMembers)
                .membershipRequestDate(requestDate)
                .build();
    }

    private GroupMemberViewDTO toMemberViewDto(GroupMember member) {
        return GroupMemberViewDTO.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .userAvatarBase64(member.getUser().getAvatar() != null ?
                        imageService.getBase64Avatar(member.getUser().getAvatar()) : null)
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    public GroupMembershipDTO toMembershipDto(Group group, GroupMember currentMember) {
        GroupMember ownerMember = group.getMembers().stream()
                .filter(member -> member.getRole() == GroupRole.OWNER)
                .findFirst()
                .orElse(null);

        boolean canCreatePosts = true;
        boolean canModerate = currentMember.getRole() == GroupRole.ADMIN ||
                currentMember.getRole() == GroupRole.OWNER ||
                currentMember.getRole() == GroupRole.MODERATOR;
        boolean canInviteMembers = currentMember.getRole() != GroupRole.MEMBER;

        return GroupMembershipDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .isPrivate(group.isPrivate())
                .category(group.getCategory())
                .avatarBase64(group.getAvatar() != null ?
                        imageService.getBase64Avatar(group.getAvatar()) : null)
                .coverBase64(group.getCover() != null ?
                        imageService.getBase64Avatar(group.getCover()) : null)
                .rules(group.getRules())
                .membersCount(group.getMembersCount())
                .postsCount(group.getPostsCount())
                .createdAt(group.getCreatedAt())
                .owner(ownerMember != null ? toMemberViewDto(ownerMember) : null)
                .userRole(currentMember.getRole())
                .joinedAt(currentMember.getJoinedAt())
                .canCreatePosts(canCreatePosts)
                .canModerate(canModerate)
                .canInviteMembers(canInviteMembers)
                .build();
    }
}