package com.example.askfm.service;

import com.example.askfm.dto.CommentDTO;
import com.example.askfm.dto.ListCommentDTO;
import com.example.askfm.exception.CommentNotFoundException;
import com.example.askfm.exception.PostNotFoundException;

import com.example.askfm.exception.UnauthorizedException;
import com.example.askfm.exception.UserNotFoundException;
import com.example.askfm.model.Comment;
import com.example.askfm.model.Post;
import com.example.askfm.model.User;
import com.example.askfm.repository.CommentRepository;
import com.example.askfm.repository.PostRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final NotificationService notificationService;

    @Transactional
    public CommentDTO createComment(Long postId, String content, String username) {
        log.debug("📝 Создание комментария к посту {} пользователем {}", postId, username);

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.debug("✨ Комментарий сохранен в БД");

        // Отправляем уведомление только если автор комментария не является автором поста
        if (!author.equals(post.getAuthor())) {
            notificationService.notifyAboutComment(author, post);
            log.debug("📨 Отправлено уведомление автору поста о новом комментарии");
        }

        return convertToDTO(savedComment);
    }


public Page<ListCommentDTO> getPostCommentsList(Long postId, Pageable pageable) {
    return commentRepository.findByPostId(postId, pageable)
            .map(this::convertToListDTO);
}

    @Transactional
    public void deleteComment(Long id, String username) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));

        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new UnauthorizedException("User " + username + " is not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }



    private CommentDTO convertToDTO(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .authorUsername(comment.getAuthor().getUsername())
                .authorAvatar(Arrays.toString(comment.getAuthor().getAvatar()))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .editedAt(comment.getEditedAt())
                .build();
    }
    private ListCommentDTO convertToListDTO(Comment comment) {
        return ListCommentDTO.builder()
                .id(comment.getId())
                .authorUsername(comment.getAuthor().getUsername())
                .content(comment.getContent())
                .authorAvatar(imageService.getBase64Avatar(comment.getAuthor().getAvatar()))
                .createdAt(comment.getCreatedAt())
                .build();
    }


}
