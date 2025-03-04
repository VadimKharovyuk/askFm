package com.example.askfm.service;


import com.example.askfm.dto.CreateGroupPostDTO;
import com.example.askfm.dto.GroupPostCommentDTO;
import com.example.askfm.dto.GroupPostDTO;

import com.example.askfm.exception.GroupNotFoundException;
import com.example.askfm.exception.PostNotFoundException;
import com.example.askfm.exception.UserNotFoundException;
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
import java.util.List;
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
    private final ImgurStorageService   imgurStorageService;

    @Transactional
    public GroupPostDTO createPost(Long groupId, String username, CreateGroupPostDTO dto) throws IOException {
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
    public void likePost(Long postId, String username) {
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (post.getLikedBy().contains(user)) {
            post.getLikedBy().remove(user);
        } else {
            post.getLikedBy().add(user);
        }

        groupPostRepository.save(post);
    }


    @Transactional
    public void deletePostById(Long groupId, String username, Long postId) throws AccessDeniedException {
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // Проверяем, что пост принадлежит указанной группе
        if (!post.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Post doesn't belong to the specified group");
        }

        // Проверяем, что пользователь является автором поста
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("User is not the author of this post");
        }

        try {
            // Удаляем изображение из Imgur, если оно есть
            if (post.getMediaDeleteHash() != null) {
                imgurStorageService.deleteImage(post.getMediaDeleteHash());
            }

            // Уменьшаем счетчик постов в группе
            Group group = post.getGroup();
            group.setPostsCount(group.getPostsCount() - 1);
            groupRepository.save(group);

            groupPostRepository.delete(post);
        } catch (Exception e) {
            log.error("Error while deleting post {} and its media: {}", postId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete post and associated media", e);
        }
    }

    public GroupPostDTO getPostById(Long postId, String username) {
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        User currentUser = null;
        if (username != null) {
            currentUser = userRepository.findByUsername(username)
                    .orElse(null);
        }

        return groupPostMapper.toDto(post, currentUser);
    }


}