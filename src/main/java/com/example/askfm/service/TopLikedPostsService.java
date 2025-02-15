package com.example.askfm.service;

import com.example.askfm.dto.PostDTO;

import com.example.askfm.maper.PostMapper;
import com.example.askfm.model.Post;
import com.example.askfm.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TopLikedPostsService {

    private static final int PREVIEW_SIZE = 3;

    private final PostRepository postRepository;
    private final PostMapper postMapper;



    @Transactional(readOnly = true)
    public Page<PostDTO> getAllTopLikedPosts(Pageable pageable, String currentUsername) {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        Page<Post> topPosts = postRepository.findTopLikedPosts(weekAgo, pageable);
        return topPosts.map(post -> postMapper.toDto(post, currentUsername));
    }

    @Transactional(readOnly = true)
    public List<PostDTO> getTopLikedPostsPreview(String currentUsername) {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        Pageable previewPageable = PageRequest.of(0, PREVIEW_SIZE);
        Page<Post> topPosts = postRepository.findTopLikedPosts(weekAgo, previewPageable);
        return postMapper.toDtoList(topPosts.getContent(), currentUsername);
    }
}