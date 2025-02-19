package com.example.askfm.service;


import com.example.askfm.dto.CreateGroupPostDTO;
import com.example.askfm.dto.GroupPostDTO;

import com.example.askfm.exception.GroupNotFoundException;
import com.example.askfm.maper.GroupPostMapper;
import com.example.askfm.model.Group;
import com.example.askfm.model.GroupMember;
import com.example.askfm.model.GroupPost;
import com.example.askfm.model.User;
import com.example.askfm.repository.GroupMemberRepository;
import com.example.askfm.repository.GroupPostRepository;
import com.example.askfm.repository.GroupRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupPostService {
    private final GroupPostRepository groupPostRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupPostMapper groupPostMapper;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public GroupPostDTO createPost(Long groupId, String username, CreateGroupPostDTO dto) throws IOException {
        log.info("Creating post in group {} for user {}", groupId, username);
        log.debug("Creating post with anonymous flag: {}", dto.isAnonymous());
        try {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new IllegalArgumentException("Группа не найдена"));

            User author = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            GroupPost post = groupPostMapper.toEntity(dto, group, author);
            post = groupPostRepository.save(post);

            // Увеличиваем счетчик постов
            group.setPostsCount(group.getPostsCount() + 1);
            groupRepository.save(group);

            log.info("Successfully created post {} in group {}", post.getId(), groupId);
            return groupPostMapper.toDto(post, author);

        } catch (Exception e) {
            log.error("Error creating post in group {}: {}", groupId, e.getMessage(), e);
            throw e;
        }
    }


    public Page<GroupPostDTO> getGroupPosts(Long groupId, String username, Pageable pageable) throws AccessDeniedException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        // Получаем текущего пользователя
        User currentUser = null;
        if (username != null) {
            currentUser = groupMemberRepository.findByGroupAndUser_Username(group, username)
                    .map(GroupMember::getUser)
                    .orElse(null);
        }

        // Проверяем доступ к постам в закрытой группе
        if (group.isPrivate() && currentUser == null) {
            throw new AccessDeniedException("This is a private group");
        }

        // Загружаем посты с пагинацией
        final User finalCurrentUser = currentUser;
        return groupPostRepository.findByGroupIdOrderByPublishedAtDesc(groupId, pageable)
                .map(post -> groupPostMapper.toDto(post, finalCurrentUser));
    }



    @Transactional
    public void likePost(Long postId, Long userId) {
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Пост не найден"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (post.getLikedBy().contains(user)) {
            post.getLikedBy().remove(user);
        } else {
            post.getLikedBy().add(user);
        }

        groupPostRepository.save(post);
    }

}