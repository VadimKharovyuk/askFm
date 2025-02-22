package com.example.askfm.controller;

import com.example.askfm.dto.*;
import com.example.askfm.enums.ReactionType;
import com.example.askfm.exception.StoryNotFoundException;
import com.example.askfm.exception.UserNotFoundException;
import com.example.askfm.model.User;
import com.example.askfm.service.StoryService;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
@Controller
@RequiredArgsConstructor
@Slf4j
public class StoryController {
    private final StoryService storyService;
    private final UserService userService;

    @GetMapping("/stories")
    public String showStories(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<StoryResponseDto> activeStories = storyService.getActiveStories();
        model.addAttribute("stories", activeStories);
        model.addAttribute("currentUsername", userDetails.getUsername());
        return "stories/list";
    }

    @GetMapping("/stories/create")
    public String showCreateStoryForm() {
        return "stories/create";
    }

    @PostMapping("/stories")
    public String createStory(@RequestParam("image") MultipartFile image,
                              @AuthenticationPrincipal UserDetails userDetails) {
        try {
            CreateStoryDto dto = CreateStoryDto.builder()
                    .username(userDetails.getUsername())
                    .imageData(image.getBytes())
                    .build();

            storyService.createStory(dto);
            return "redirect:/stories";
        } catch (IOException e) {
            log.error("Error processing image upload", e);
            return "redirect:/stories/create?error";
        }
    }

    @PostMapping("/stories/{storyId}/view")
    @ResponseBody
    public ResponseEntity<Void> addView(@PathVariable Long storyId,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        storyService.addView(storyId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stories/{storyId}/react")
    @ResponseBody
    public ResponseEntity<Void> addReaction(
            @PathVariable Long storyId,
            @RequestParam ReactionType reactionType,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        storyService.addReaction(storyId, currentUser.getId(), reactionType);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/stories/{storyId}")
    @ResponseBody
    @PreAuthorize("@storyService.isOwner(#storyId, authentication.name)")
    public ResponseEntity<Void> deleteStory(@PathVariable Long storyId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!storyService.isOwner(storyId, currentUser.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            storyService.deleteStory(storyId);
            return ResponseEntity.ok().build();
        } catch (StoryNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/stories/{storyId}/views")
    public String getStoryViews(@PathVariable Long storyId, Model model) {
        List<StoryViewDto> views = storyService.getStoryViews(storyId);
        model.addAttribute("views", views);
        return "stories/views";
    }

    @GetMapping("/stories/{storyId}/reactions")
    public String getStoryReactions(@PathVariable Long storyId, Model model) {
        List<StoryReactionDto> reactions = storyService.getStoryReactions(storyId);
        model.addAttribute("reactions", reactions);
        return "stories/reactions";
    }

    /**
     * Показать все истории текущего пользователя
     */
    @GetMapping("/stories/my")
    public String showMyStories(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<StoryResponseDto> userStories = storyService.getUserStories(userDetails.getUsername());
        model.addAttribute("stories", userStories);
        model.addAttribute("currentUsername", userDetails.getUsername());
        return "stories/list";
    }

    /**
     * Показать активные истории текущего пользователя
     */
    @GetMapping("/stories/my/active")
    public String showMyActiveStories(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<StoryResponseDto> activeStories = storyService.getUserActiveStories(userDetails.getUsername());
        model.addAttribute("stories", activeStories);
        model.addAttribute("currentUsername", userDetails.getUsername());
        return "stories/list";
    }

    /**
     * Показать истории конкретного пользователя
     */
    @GetMapping("/stories/user/{username}")
    public String showUserStories(@PathVariable String username,
                                  Model model,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        List<StoryResponseDto> userStories = storyService.getUserActiveStories(username);
        UserProfileDto userProfile = storyService.getUserProfile(username);

        model.addAttribute("stories", userStories);
        model.addAttribute("userProfile", userProfile);
        model.addAttribute("currentUsername", userDetails.getUsername());
        model.addAttribute("isOwnProfile", username.equals(userDetails.getUsername()));

        return "stories/user-stories";
    }

}