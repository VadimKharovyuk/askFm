package com.example.askfm.service;

import com.example.askfm.dto.CreateCommentDTO;
import com.example.askfm.dto.GroupPostCommentDTO;
import com.example.askfm.exception.CommentNotFoundException;
import com.example.askfm.exception.PostNotFoundException;
import com.example.askfm.exception.UserNotFoundException;
import com.example.askfm.maper.GroupPostCommentMapper;
import com.example.askfm.model.GroupPost;
import com.example.askfm.model.GroupPostComment;
import com.example.askfm.model.User;
import com.example.askfm.repository.GroupPostCommentRepository;
import com.example.askfm.repository.GroupPostRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupPostCommentService {
    private final GroupPostCommentRepository commentRepository;
    private final GroupPostRepository postRepository;
    private final UserRepository userRepository;
    private final GroupPostCommentMapper commentMapper;

    @Transactional
    public GroupPostCommentDTO createComment(Long postId, String username, CreateCommentDTO dto) {
        log.info("Creating comment for post {} by user {}", postId, username);

        GroupPost post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        GroupPostComment comment = commentMapper.toEntity(dto, post, author);
        comment = commentRepository.save(comment);

        log.info("Created comment {} for post {}", comment.getId(), postId);
        return commentMapper.toDto(comment);
    }

    public List<GroupPostCommentDTO> getPostComments(Long postId) {
        log.info("Getting comments for post {}", postId);

        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId)
                .stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId, String username) {
        log.info("Deleting comment {} by user {}", commentId, username);

        GroupPostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        // Проверяем, является ли пользователь автором комментария
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("User is not the author of this comment");
        }

        commentRepository.delete(comment);
        log.info("Deleted comment {}", commentId);
    }
}
