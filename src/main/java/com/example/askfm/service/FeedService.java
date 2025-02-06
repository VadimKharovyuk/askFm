package com.example.askfm.service;

import com.example.askfm.dto.PostDTO;
import com.example.askfm.maper.PostMapper;
import com.example.askfm.model.Post;
import com.example.askfm.model.Tag;
import com.example.askfm.model.User;
import com.example.askfm.repository.PostRepository;
import com.example.askfm.repository.SubscriptionRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.yaml.snakeyaml.tokens.Token.ID.Tag;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FeedService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ImageService imageService;


    @Transactional(readOnly = true)
    public Page<PostDTO> getFeedPosts(String username, Pageable pageable) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<Long> followingIds = subscriptionRepository.findBySubscriber(currentUser)
                .stream()
                .map(subscription -> subscription.getSubscribedTo().getId())
                .collect(Collectors.toList());

        if (followingIds.isEmpty()) {

            return Page.empty(pageable);
        }

        Page<Post> posts = postRepository.findByAuthorIdInOrderByPublishedAtDesc(followingIds, pageable);

        return posts.map(post -> PostDTO.builder()
                .id(post.getId())
                .authorUsername(post.getAuthor().getUsername())
                .authorAvatar(post.getAuthor().getAvatar() != null ?
                        "data:image/jpeg;base64," + imageService.getBase64Avatar(post.getAuthor().getAvatar()) :
                        null)
                .content(post.getContent())
                .base64Media(post.getMedia() != null ?
                        "data:image/jpeg;base64," + imageService.getBase64Avatar(post.getMedia()) :
                        null)
                .publishedAt(post.getPublishedAt())
                .views((long) post.getViews().size())
                .likesCount(post.getLikedBy().size())
                .commentsCount((long) post.getComments().size())
                .repostsCount(post.getReposts().size())
                .tags(post.getTags().stream()
                        .map(com.example.askfm.model.Tag::getName)
                        .collect(Collectors.toSet()))
                .build()
        );
    }

}
