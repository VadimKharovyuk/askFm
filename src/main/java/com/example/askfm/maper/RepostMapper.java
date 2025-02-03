package com.example.askfm.maper;

import com.example.askfm.dto.CreateRepostRequest;
import com.example.askfm.dto.RepostDTO;
import com.example.askfm.model.Repost;
import com.example.askfm.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@RequiredArgsConstructor
@Component
public class RepostMapper {
    private final PostService postService;

    public RepostDTO toDto(Repost repost) {
        if (repost == null) {
            return null;
        }

        return RepostDTO.builder()
                .id(repost.getId())
                .userId(repost.getUser() != null ? repost.getUser().getId() : null)
                .username(repost.getUser() != null ? repost.getUser().getUsername() : null)
                .originalPostId(repost.getOriginalPost() != null ? repost.getOriginalPost().getId() : null)
                .authorUsername(repost.getOriginalPost() != null ? repost.getOriginalPost().getAuthor().getUsername() : null)
                .repostedAt(repost.getRepostedAt())
                .originalPostedAt(repost.getOriginalPost() != null ? repost.getOriginalPost().getPublishedAt() : null)
                .originalPost(repost.getOriginalPost() != null ?
                        postService.getPostDTO(repost.getOriginalPost(), repost.getUser().getUsername()) : null)
                .build();
    }

    public Repost toEntity(CreateRepostRequest request) {
        if (request == null) {
            return null;
        }

        Repost repost = new Repost();
        repost.setRepostedAt(LocalDateTime.now());

        return repost;
    }


    public List<RepostDTO> toDtoList(List<Repost> reposts) {
        if (reposts == null) {
            return null;
        }

        List<RepostDTO> list = new ArrayList<>(reposts.size());
        for (Repost repost : reposts) {
            list.add(toDto(repost));
        }

        return list;
    }
}