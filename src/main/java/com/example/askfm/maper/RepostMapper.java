package com.example.askfm.maper;//package com.example.askfm.maper;
//
//import com.example.askfm.dto.CreateRepostRequest;
//import com.example.askfm.dto.RepostDTO;
//import com.example.askfm.model.Repost;
//import com.example.askfm.service.PostService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//@RequiredArgsConstructor
//@Component
//public class RepostMapper {
//    private final PostService postService;
//
//    public RepostDTO toDto(Repost repost) {
//        if (repost == null) {
//            return null;
//        }
//
//        return RepostDTO.builder()
//                .id(repost.getId())
//                .userId(repost.getUser() != null ? repost.getUser().getId() : null)
//                .username(repost.getUser() != null ? repost.getUser().getUsername() : null)
//                .originalPostId(repost.getOriginalPost() != null ? repost.getOriginalPost().getId() : null)
//                .authorUsername(repost.getOriginalPost() != null ? repost.getOriginalPost().getAuthor().getUsername() : null)
//                .repostedAt(repost.getRepostedAt())
//                .originalPostedAt(repost.getOriginalPost() != null ? repost.getOriginalPost().getPublishedAt() : null)
//                .originalPost(repost.getOriginalPost() != null ?
//                        postService.getPostDTO(repost.getOriginalPost(), repost.getUser().getUsername()) : null)
//                .build();
//    }
//
//    public Repost toEntity(CreateRepostRequest request) {
//        if (request == null) {
//            return null;
//        }
//
//        Repost repost = new Repost();
//        repost.setRepostedAt(LocalDateTime.now());
//
//        return repost;
//    }
//
//
//    public List<RepostDTO> toDtoList(List<Repost> reposts) {
//        if (reposts == null) {
//            return null;
//        }
//
//        List<RepostDTO> list = new ArrayList<>(reposts.size());
//        for (Repost repost : reposts) {
//            list.add(toDto(repost));
//        }
//
//        return list;
//    }
//}

import com.example.askfm.dto.PostDTO;
import com.example.askfm.dto.RepostDTO;
import com.example.askfm.model.Post;
import com.example.askfm.model.Repost;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RepostMapper {

    private final PostMapper postMapper;

    public RepostMapper(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    public RepostDTO toDto(Repost repost, String currentUsername) {
        if (repost == null) {
            return null;
        }

        Post originalPost = repost.getOriginalPost();
        PostDTO originalPostDto = postMapper.toDto(originalPost, currentUsername);

        return RepostDTO.builder()
                .id(repost.getId())
                .userId(repost.getUser().getId())
                .username(repost.getUser().getUsername())
                .originalPostId(originalPost.getId())
                .repostedAt(repost.getRepostedAt())
                .originalPostedAt(originalPost.getPublishedAt())
                .authorUsername(originalPost.getAuthor().getUsername())
                .originalPost(originalPostDto)
                .build();
    }

    public Repost toEntity(RepostDTO dto) {
        if (dto == null) {
            return null;
        }

        return Repost.builder()
                .id(dto.getId())
                .repostedAt(dto.getRepostedAt())
                .build();
    }

    public List<RepostDTO> toDtoList(List<Repost> reposts, String currentUsername) {
        if (reposts == null) {
            return Collections.emptyList();
        }
        return reposts.stream()
                .map(repost -> toDto(repost, currentUsername))
                .collect(Collectors.toList());
    }
}