package com.example.askfm.controller;

import com.example.askfm.dto.*;
import com.example.askfm.model.User;
import com.example.askfm.model.UserProfile;
import com.example.askfm.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


@Controller
@RequiredArgsConstructor
public class UserProfileController {
    private final UserService userService;
    private final QuestionService questionService;
    private final SubscriptionService subscriptionService;
    private final UserProfileService userProfileService;
    private final ImageService imageService;
    private final SuggestedUsersService suggestedUsersService;
    private final PostService postService;
    private final CommentService commentService;
    private final VisitService visitService;




    @GetMapping("/users/{username}")
    public String showUserProfile(@PathVariable String username,
                                  @AuthenticationPrincipal UserDetails currentUser,
                                  Model model) {
        User user = userService.findByUsername(username);
        User recipient = userService.findByUsername(username);


        String currentUsername = currentUser != null ? currentUser.getUsername() : null;
        List<PostDTO> userPosts = postService.getUserPosts(username, currentUsername);
        model.addAttribute("posts", userPosts);

        String joinDate = user.getCreatedAt()
                .format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("ru")));

        // Безопасное получение местоположения
        String location = null;
        if (user.getProfile() != null) {
            location = user.getProfile().getLocation();
        }

        // Безопасное получение местоположения
        String bio = null;
        if (user.getProfile() != null) {
            bio = user.getProfile().getBio();
        }

        // Безопасное получение местоположения
        String website = null;
        if (user.getProfile() != null) {
            website = user.getProfile().getWebsite();
        }

// Получаем количество просмотров для каждого поста
//        List<PostDTO> userPostsViews = postService.getUserPostsWiews(username, currentUsername);
//        model.addAttribute("posts", userPostsViews);


        // Получение рекомендуемых пользователей
        List<UserSuggestionDTO> suggestedUsers = Collections.emptyList();
        if (currentUser != null) {
            suggestedUsers = suggestedUsersService.getSuggestedUsers(currentUser.getUsername());
        }

        // Получаем список вопросов для пользователя
        List<QuestionResponseDto> questions = questionService.getQuestionsForUser(user.getId());

        // Создаем DTO с уже установленным recipientId
        QuestionRequestDto questionRequest = new QuestionRequestDto();
        questionRequest.setRecipientId(recipient.getId());

        boolean isFollowing = false;
        if (currentUser != null) {
            isFollowing = subscriptionService.isFollowing(currentUser.getUsername(), username);
        }


        if (currentUser != null) {
            User visitor = userService.findByUsername(currentUser.getUsername());
            visitService.recordVisit(visitor, user);
        }

        // Существующие атрибуты
        model.addAttribute("profileUser", user);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("followersCount", subscriptionService.getSubscribersCount(username));
        model.addAttribute("followingCount", subscriptionService.getSubscribersCount(username));
        model.addAttribute("questionRequest", questionRequest);
        model.addAttribute("currentUser", currentUser != null ? currentUser.getUsername() : null);
        model.addAttribute("avatarBase64", imageService.getBase64Avatar(user.getAvatar()));
        model.addAttribute("coverBase64", imageService.getBase64Avatar(user.getCover()));
        model.addAttribute("questions", questions);
        model.addAttribute("joinDate", joinDate);
        model.addAttribute("getLocation", location);
        model.addAttribute("bio", bio);
        model.addAttribute("website", website);

        // Добавляем рекомендуемых пользователей
        model.addAttribute("suggestedUsers", suggestedUsers);

        return "user/profile-view";
    }



    @GetMapping("/profile/edit")
    public String showEditProfileForm(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        User user = userService.findByUsername(currentUser.getUsername());
        model.addAttribute("profileDTO", userProfileService.getProfileDTO(user));
        return "user/edit-profile";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute ProfileEditDTO profileDTO,
                                BindingResult result,
                                @AuthenticationPrincipal UserDetails currentUser) {
        if (result.hasErrors()) {
            return "user/edit-profile";
        }

        userProfileService.updateProfile(currentUser.getUsername(), profileDTO);
        return "redirect:/users/" + currentUser.getUsername();
    }

    @GetMapping("/users/{username}/info")
    public String showUserInfo(@PathVariable String username, Model model,
                               @AuthenticationPrincipal UserDetails currentUser) {
        User user = userService.findByUsername(username);
        UserProfile userProfile = userProfileService.getUserProfile(user);

        model.addAttribute("profileUser", user);
        model.addAttribute("userProfile", userProfile);
        model.addAttribute("currentUser", currentUser != null ? currentUser.getUsername() : null);

        return "user/user-info";
    }
}
