
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
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
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Post originalPost = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new PostNotFoundException(request.getPostId()));

        validateRepostCreation(user, originalPost);

        Repost repost = buildRepost(user, originalPost);
        Repost savedRepost = repostRepository.save(repost);
        return repostMapper.toDto(savedRepost, user.getUsername());
    }

    public void deleteRepost(Long userId, Long postId) {

        Repost repost = repostRepository.findByUserIdAndOriginalPostId(userId, postId)
                .orElseThrow(() -> new RepostNotFoundException("Repost not found"));

        validateRepostDeletion(repost, userId);

        repostRepository.delete(repost);
        log.debug("Successfully deleted repost");
    }

    private void validateRepostCreation(User user, Post originalPost) {
        boolean alreadyReposted = repostRepository.existsByUserAndOriginalPost(user, originalPost);
        if (alreadyReposted) {
            throw new AlreadyRepostedException("You have already reposted this post");
        }
    }

    private void validateRepostDeletion(Repost repost, Long userId) {
        if (!repost.getUser().getId().equals(userId)) {
            log.warn("Unauthorized delete attempt. Repost user: {}, Request user: {}",
                    repost.getUser().getId(), userId);
            throw new UnauthorizedException("Not authorized to delete this repost");
        }
    }

    private Repost buildRepost(User user, Post originalPost) {
        return Repost.builder()
                .user(user)
                .originalPost(originalPost)
                .repostedAt(LocalDateTime.now())
                .build();
    }
}



//package com.example.askfm.service;
//
//import com.example.askfm.dto.CreateRepostRequest;
//import com.example.askfm.dto.RepostDTO;
//import com.example.askfm.exception.*;
//import com.example.askfm.maper.RepostMapper;
//import com.example.askfm.model.Post;
//import com.example.askfm.model.Repost;
//import com.example.askfm.model.User;
//import com.example.askfm.repository.PostRepository;
//import com.example.askfm.repository.RepostRepository;
//import com.example.askfm.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//@Slf4j
//@Service
//@Transactional
//@RequiredArgsConstructor
//public class RepostService {
//    private final RepostRepository repostRepository;
//    private final PostRepository postRepository;
//    private final UserRepository userRepository;
//    private final RepostMapper repostMapper;
//
//    public RepostDTO createRepost(Long userId, CreateRepostRequest request) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new UserNotFoundException("User not found"));
//
//        Post originalPost = postRepository.findById(request.getPostId())
//                .orElseThrow(() -> new PostNotFoundException(request.getPostId()));
//
//        // Check if already reposted
//        boolean alreadyReposted = repostRepository.existsByUserAndOriginalPost(user, originalPost);
//        if (alreadyReposted) {
//            throw new AlreadyRepostedException("You have already reposted this post");
//        }
//
//        Repost repost = Repost.builder()
//                .user(user)
//                .originalPost(originalPost)
//                .repostedAt(LocalDateTime.now())
//
//                .build();
//
//        return repostMapper.toDto(repostRepository.save(repost));
//    }
//
//    public void deleteRepost(Long userId, Long postId) {
//        // Находим репост по userId и postId оригинального поста
//        Repost repost = repostRepository.findByUserIdAndOriginalPostId(userId, postId)
//                .orElseThrow(() -> {
//                    return new RepostNotFoundException("Repost not found");
//                });
//        if (!repost.getUser().getId().equals(userId)) {
//            log.warn("Unauthorized delete attempt. Repost user: {}, Request user: {}",
//                    repost.getUser().getId(), userId);
//            throw new UnauthorizedException("Not authorized to delete this repost");
//        }
//        repostRepository.delete(repost);
//    }
//
//}
