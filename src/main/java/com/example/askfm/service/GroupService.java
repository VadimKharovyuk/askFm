package com.example.askfm.service;

import com.example.askfm.dto.CreateGroupDTO;
import com.example.askfm.dto.GroupListDTO;
import com.example.askfm.dto.GroupMembershipDTO;
import com.example.askfm.dto.GroupViewDTO;
import com.example.askfm.enums.GroupRole;
import com.example.askfm.enums.JoinRequestStatus;
import com.example.askfm.enums.MembershipStatus;
import com.example.askfm.exception.GroupNotFoundException;
import com.example.askfm.exception.NotGroupMemberException;
import com.example.askfm.exception.UserNotFoundException;
import com.example.askfm.maper.GroupMapper;
import com.example.askfm.model.Group;
import com.example.askfm.model.GroupJoinRequest;
import com.example.askfm.model.GroupMember;
import com.example.askfm.model.User;
import com.example.askfm.repository.GroupJoinRequestRepository;
import com.example.askfm.repository.GroupMemberRepository;
import com.example.askfm.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupMapper groupMapper;
    private final GroupRepository groupRepository;
    private final UserService userService;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupJoinRequestRepository joinRequestRepository;

    @Transactional
    public Group createGroup(CreateGroupDTO dto, String username) throws IOException {
        User owner = userService.findByUsername(username);
        if (owner == null) {
            throw new UserNotFoundException("User not found with username: " + username);
        }

        Group group = groupMapper.toEntity(dto, owner);
        return groupRepository.save(group);
    }

    public Page<GroupListDTO> getAllGroups(Pageable pageable, String username) {
        Page<Group> groups = groupRepository.findAll(pageable);
        return groups.map(group -> {
            GroupListDTO dto = groupMapper.toListDto(group);
            if (username != null) {
                // Проверяем, является ли пользователь участником группы
                boolean isMember = groupMemberRepository
                        .findByGroupAndUser_Username(group, username)
                        .isPresent();
                dto.setMember(isMember);
            }
            return dto;
        });
    }
    public GroupViewDTO getGroupById(Long groupId, String currentUsername) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        MembershipStatus status = MembershipStatus.NOT_MEMBER;
        LocalDateTime requestDate = null;
        GroupRole userRole = null; // Добавляем роль пользователя

        if (currentUsername != null) {
            Optional<GroupMember> member = groupMemberRepository
                    .findByGroupAndUser_Username(group, currentUsername);

            if (member.isPresent()) {
                status = MembershipStatus.MEMBER;
                userRole = member.get().getRole(); // Сохраняем роль пользователя
            } else {
                Optional<GroupJoinRequest> request = joinRequestRepository
                        .findByGroupAndUser_UsernameAndStatus(
                                group, currentUsername, JoinRequestStatus.PENDING);

                if (request.isPresent()) {
                    status = MembershipStatus.PENDING;
                    requestDate = request.get().getCreatedAt();
                }
            }
        }

        // Передаем роль пользователя в маппер
        return groupMapper.toViewDto(group, status, requestDate, userRole);
    }

    @Transactional
    public void joinGroup(Long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));
        User user = userService.findByUsername(username);

        if (groupMemberRepository.findByGroupAndUser_Username(group, username).isPresent()) {
            throw new IllegalStateException("User is already a member of this group");
        }

        if (joinRequestRepository.findByGroupAndUser_UsernameAndStatus(
                group, username, JoinRequestStatus.PENDING).isPresent()) {
            throw new IllegalStateException("Join request already exists");
        }

        if (group.isPrivate()) {
            GroupJoinRequest request = GroupJoinRequest.builder()
                    .group(group)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .status(JoinRequestStatus.PENDING)
                    .build();
            joinRequestRepository.save(request);
        } else {
            addMemberToGroup(group, user, GroupRole.MEMBER);
        }
    }

    @Transactional
    public void approveJoinRequest(Long requestId, String approverUsername) {
        GroupJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new GroupNotFoundException("Join request not found"));

        GroupMember approver = groupMemberRepository
                .findByGroupAndUser_Username(request.getGroup(), approverUsername)
                .filter(member -> canModerateGroup(request.getGroup().getId(), approverUsername))
                .orElseThrow(() -> new AccessDeniedException("No permission to approve requests"));

        addMemberToGroup(request.getGroup(), request.getUser(), GroupRole.MEMBER);

        request.setStatus(JoinRequestStatus.APPROVED);
        joinRequestRepository.save(request);
    }

    @Transactional
    public void rejectJoinRequest(Long requestId, String approverUsername) {
        GroupJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new GroupNotFoundException("Join request not found"));

        if (!canModerateGroup(request.getGroup().getId(), approverUsername)) {
            throw new AccessDeniedException("No permission to reject requests");
        }

        request.setStatus(JoinRequestStatus.REJECTED);
        joinRequestRepository.save(request);
    }

    @Transactional
    public void leaveGroup(Long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        GroupMember member = groupMemberRepository.findByGroupAndUser_Username(group, username)
                .orElseThrow(() -> new NotGroupMemberException("User is not a member of this group"));

        if (member.getRole() == GroupRole.OWNER) {
            throw new IllegalStateException("Owner cannot leave the group");
        }

        groupMemberRepository.delete(member);
        group.setMembersCount(group.getMembersCount() - 1);
        groupRepository.save(group);
    }

    public GroupMembershipDTO getGroupForMember(Long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        GroupMember member = groupMemberRepository.findByGroupAndUser_Username(group, username)
                .orElseThrow(() -> new NotGroupMemberException("User is not a member of this group"));

        return groupMapper.toMembershipDto(group, member);
    }

    public List<GroupJoinRequest> getPendingRequests(Long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        if (!canModerateGroup(groupId, username)) {
            throw new AccessDeniedException("No permission to view requests");
        }

        return joinRequestRepository.findByGroupAndStatus(group, JoinRequestStatus.PENDING);
    }

    public boolean canModerateGroup(Long groupId, String username) {
        try {
            GroupMembershipDTO membership = getGroupForMember(groupId, username);
            return membership.isCanModerate();
        } catch (NotGroupMemberException e) {
            return false;
        }
    }

    @Transactional
    protected void addMemberToGroup(Group group, User user, GroupRole role) {
        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();

        groupMemberRepository.save(member);
        group.setMembersCount(group.getMembersCount() + 1);
        groupRepository.save(group);
    }

    public Page<GroupListDTO> getMyGroups(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found with username: " + username);
        }

        // Получаем все группы, где пользователь является участником
        Page<Group> groups = groupRepository.findByMembersUserUsername(username, pageable);
        return groups.map(group -> {
            GroupListDTO dto = groupMapper.toListDto(group);
            dto.setMember(true); // Это точно группы пользователя
            return dto;
        });
    }
    public Page<GroupListDTO> getManagedGroups(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found with username: " + username);
        }

        // Получаем группы, где пользователь является владельцем, админом или модератором
        Page<Group> groups = groupRepository.findByMembersUserUsernameAndMembersRoleIn(
                username,
                List.of(GroupRole.OWNER, GroupRole.ADMIN, GroupRole.MODERATOR),
                pageable
        );

        return groups.map(group -> {
            GroupListDTO dto = groupMapper.toListDto(group);
            dto.setMember(true);
            return dto;
        });
    }
}