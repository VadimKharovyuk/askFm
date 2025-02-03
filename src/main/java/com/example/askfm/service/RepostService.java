package com.example.askfm.service;

import com.example.askfm.dto.CreateRepostRequest;
import com.example.askfm.dto.RepostDTO;
import com.example.askfm.exception.*;
import com.example.askfm.maper.RepostMapper;
import com.example.askfm.model.Post;
import com.example.askfm.model.Repost;
import com.example.askfm.model.User;
import com.example.askfm.repository.PostRepository;
import com.example.askfm.repository.RepostRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RepostService {
    private final RepostRepository repostRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RepostMapper repostMapper;

    public RepostDTO createRepost(Long userId, CreateRepostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Post originalPost = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new PostNotFoundException(request.getPostId()));

        // Check if already reposted
        boolean alreadyReposted = repostRepository.existsByUserAndOriginalPost(user, originalPost);
        if (alreadyReposted) {
            throw new AlreadyRepostedException("You have already reposted this post");
        }

        Repost repost = Repost.builder()
                .user(user)
                .originalPost(originalPost)
                .repostedAt(LocalDateTime.now())
                .build();

        return repostMapper.toDto(repostRepository.save(repost));
    }

    public void deleteRepost(Long userId, Long repostId) {
        Repost repost = repostRepository.findById(repostId)
                .orElseThrow(() -> new RepostNotFoundException("Repost not found"));

        if (!repost.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not authorized to delete this repost");
        }

        repostRepository.delete(repost);
    }

    public List<RepostDTO> getUserReposts(Long userId, Pageable pageable) {
        return repostMapper.toDtoList(
                repostRepository.findByUserId(userId, pageable)
        );
    }

    public List<RepostDTO> getPostReposts(Long postId, Pageable pageable) {
        return repostMapper.toDtoList(
                repostRepository.findByOriginalPostId(postId, pageable)
        );
    }
}