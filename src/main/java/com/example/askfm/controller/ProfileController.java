package com.example.askfm.controller;

import com.example.askfm.dto.AdLeadFormDTO;
import com.example.askfm.dto.AdPublicDto;
import com.example.askfm.dto.PostCreateDTO;
import com.example.askfm.dto.UpcomingBirthdayDTO;
import com.example.askfm.model.Post;
import com.example.askfm.model.User;
import com.example.askfm.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    private final UserService userService;
    private final ImageService imageService;
    private final PostService postService;
    private final BirthdayService birthdayService;
    private final FeedService feedService;
    private final AdService adService;




    @GetMapping("/home")
    public String home(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        User user = userService.findByUsername(userDetails.getUsername());
        String username = userDetails.getUsername();

        AdPublicDto ad = adService.getRandomAd();
        // Создаем DTO для формы
        AdLeadFormDTO leadFormDTO = new AdLeadFormDTO();
        leadFormDTO.setUsername(user.getUsername());
        leadFormDTO.setEmail(user.getEmail());

        model.addAttribute("ad", ad);
        model.addAttribute("leadForm", leadFormDTO);


        // Получаем дни рождения на сегодня
        List<UpcomingBirthdayDTO> todaysBirthdays = birthdayService.getTodaysBirthdays(username);
        // Получаем предстоящие дни рождения
        List<UpcomingBirthdayDTO> upcomingBirthdays = birthdayService.getUpcomingBirthdays(username, 7);

        // Создаем объект Pageable и получаем посты
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Post> feedPosts = feedService.getFeedPosts(username, pageable);

        model.addAttribute("todaysBirthdays", todaysBirthdays);
        model.addAttribute("upcomingBirthdays", upcomingBirthdays);
        model.addAttribute("postForm", new PostCreateDTO());
        model.addAttribute("userPosts", postService.getUserPosts(username, username));
        model.addAttribute("feedPosts", feedPosts);
        model.addAttribute("username", username);
        model.addAttribute("profileUser", user);
        model.addAttribute("currentUser", username);
        model.addAttribute("coverBase64", imageService.getBase64Avatar(user.getCover()));
        model.addAttribute("avatarBase64", imageService.getBase64Avatar(user.getAvatar()));

        // Добавляем информацию о пагинации
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", feedPosts.getTotalPages());
        model.addAttribute("totalItems", feedPosts.getTotalElements());
        // Добавляем флаги для навигации
        model.addAttribute("hasNext", feedPosts.hasNext());
        model.addAttribute("hasPrevious", feedPosts.hasPrevious());

        return "user/profile";
    }


    @PostMapping("/profile/avatar")
    public String updateAvatar(@RequestParam("avatar") MultipartFile file,
                               @AuthenticationPrincipal UserDetails currentUser) {
        try {
            if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
                throw new IllegalArgumentException("Разрешены только изображения");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Размер файла должен быть меньше 5MB");
            }

            byte[] resizedImage = imageService.resizeImage(file.getBytes(), 300);
            userService.updateAvatar(currentUser.getUsername(), resizedImage);

            return "redirect:/home";
        } catch (IOException e) {
            return "redirect:/home?error=true";
        }


    }

    @PostMapping("/profile/cover")
    public String updateCover(@RequestParam("cover") MultipartFile file,
                              @AuthenticationPrincipal UserDetails currentUser) {
        try {
            if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
                throw new IllegalArgumentException("Разрешены только изображения");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Размер файла должен быть меньше 5MB");
            }

            byte[] resizedImage = imageService.resizeImage(file.getBytes(), 600);
            userService.updateCover(currentUser.getUsername(), resizedImage);

            return "redirect:/home";
        } catch (IOException e) {
            return "redirect:/home?error=true";
        }

    }
}