package com.example.askfm.maper;

import com.example.askfm.dto.*;
import com.example.askfm.enums.ReactionType;
import com.example.askfm.model.Story;
import com.example.askfm.model.StoryReaction;
import com.example.askfm.model.StoryView;
import com.example.askfm.model.User;
import com.example.askfm.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
public class StoryMapper {
    private final ImageService imageService;

    public UserProfileDto toUserProfileDto(User user, List<Story> stories) {
        if (user == null) {
            return null;
        }

        return UserProfileDto.builder()
                .username(user.getUsername())
                .avatarBase64(user.getAvatar() != null ? imageService.getBase64Avatar(user.getAvatar()) : null)
                .storiesCount(stories != null ? stories.size() : 0)
                .build();
    }
    public StoryResponseDto toResponseDto(Story story) {
        if (story == null) {
            return null;
        }

        return StoryResponseDto.builder()
                .id(story.getId())
                .username(story.getUser().getUsername())
                .imageUrl(story.getImageUrl())
                .userAvatar(story.getUser().getAvatar() != null ?
                        imageService.getBase64Avatar(story.getUser().getAvatar()) : null)
                .createdAt(story.getCreatedAt())
                .viewsCount(story.getViews() != null ? story.getViews().size() : 0)
                .reactionCounts(calculateReactionCounts(story.getReactions()))
                .build();
    }

        public StoryViewDto toViewDto(StoryView view) {
            if (view == null) {
                return null;
            }

            return StoryViewDto.builder()
                    .username(view.getUser().getUsername())
                    .viewedAt(view.getViewedAt())
                    .userAvatar(view.getUser().getAvatar() != null ?
                            imageService.getBase64Avatar(view.getUser().getAvatar()) :
                            null)
                    .build();
        }

        public StoryReactionDto toReactionDto(StoryReaction reaction) {
            if (reaction == null) {
                return null;
            }

            return StoryReactionDto.builder()
                    .username(reaction.getUser().getUsername())
                    .reactionType(reaction.getReactionType())
                    .emoji(reaction.getReactionType().getEmoji())
                    .reactedAt(reaction.getReactedAt())
                    .userAvatar(reaction.getUser().getAvatar() != null ?
                            imageService.getBase64Avatar(reaction.getUser().getAvatar()) :
                            null)
                    .build();
        }

        public List<StoryViewDto> toViewDtoList(List<StoryView> views) {
            if (views == null) {
                return Collections.emptyList();
            }

            return views.stream()
                    .map(this::toViewDto)
                    .collect(Collectors.toList());
        }

        public List<StoryReactionDto> toReactionDtoList(List<StoryReaction> reactions) {
            if (reactions == null) {
                return Collections.emptyList();
            }

            return reactions.stream()
                    .map(this::toReactionDto)
                    .collect(Collectors.toList());
        }

        private List<ReactionCountDto> calculateReactionCounts(List<StoryReaction> reactions) {
            List<StoryReaction> safeReactions = reactions != null ? reactions : Collections.emptyList();

            Map<ReactionType, Long> countsByType = safeReactions.stream()
                    .collect(Collectors.groupingBy(
                            StoryReaction::getReactionType,
                            Collectors.counting()
                    ));

            return Arrays.stream(ReactionType.values())
                    .map(type -> ReactionCountDto.builder()
                            .type(type)
                            .emoji(type.getEmoji())
                            .count(countsByType.getOrDefault(type, 0L))
                            .build())
                    .collect(Collectors.toList());
        }
    }
