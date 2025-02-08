package com.example.askfm.controller;

import com.example.askfm.dto.CreatePhotoRequest;
import com.example.askfm.dto.PhotoDTO;
import com.example.askfm.model.Photo;
import com.example.askfm.model.User;
import com.example.askfm.service.PhotoService;
import com.example.askfm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
@Controller
@RequestMapping("/photos")
@RequiredArgsConstructor
public class PhotoController {
    private final PhotoService photoService;
    private final UserService userService;

    @GetMapping("/upload")
    public String showUploadForm(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        model.addAttribute("photoRequest", new CreatePhotoRequest());

        Pageable pageable = PageRequest.of(page, size);
        Page<PhotoDTO> userPhotos = photoService.getUserPhotosByUsername(username, pageable);

        model.addAttribute("userPhotos", userPhotos.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPhotos.getTotalPages());
        model.addAttribute("username", username);

        return "photos/upload";
    }

    @ModelAttribute("isPhotoUnlocked")
    public Function<PhotoDTO, Boolean> isPhotoUnlocked(@AuthenticationPrincipal UserDetails currentUser) {
        String username = currentUser != null ? currentUser.getUsername() : null;
        return photoService.createPhotoUnlockChecker(username);
    }

    @PostMapping("/upload")
    public String uploadPhoto(
            @Valid @ModelAttribute CreatePhotoRequest photoRequest,
            @RequestParam("image") MultipartFile image,
            BindingResult bindingResult,  // Добавить для валидации
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    "Invalid input: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/photos/upload";
        }

        try {
            if (image.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select an image to upload");
                return "redirect:/photos/upload";
            }

            String username = userDetails.getUsername();
            PhotoDTO createdPhoto = photoService.createPhoto(
                    photoRequest,
                    username,
                    image.getBytes()
            );

            redirectAttributes.addFlashAttribute("success", "Photo uploaded successfully");
            return "redirect:/photos/user/" + username + "/photos";

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to process image: " + e.getMessage());
            return "redirect:/photos/upload";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/photos/upload";
        }
    }

    @GetMapping("/user/{username}/photos")
    public String showUserPhotoGrid(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal UserDetails currentUser,
            Model model
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PhotoDTO> photos = photoService.getUserPhotosByUsername(username, pageable);
            User photoOwner = userService.findByUsername(username);

            model.addAttribute("photos", photos);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", photos.getTotalPages());
            model.addAttribute("photoOwner", photoOwner);
            model.addAttribute("currentUsername",
                    currentUser != null ? currentUser.getUsername() : null);

            return "photos/grid";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Failed to load photos: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/{photoId}/unlock")
    public String unlockPhoto(
            @PathVariable Long photoId,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to unlock photos");
            return "redirect:/login";
        }

        try {
            PhotoDTO unlockedPhoto = photoService.unlockPhoto(photoId, currentUser.getUsername());
            redirectAttributes.addFlashAttribute("success",
                    "Photo unlocked successfully! You can now view the full content.");

            Photo photo = photoService.getPhotoEntity(photoId);
            return "redirect:/photos/user/" + photo.getOwner().getUsername() + "/photos";

        } catch (RuntimeException e) {
            String errorMessage = switch (e.getMessage()) {
                case "Insufficient balance to unlock photo" ->
                        "You don't have enough coins to unlock this photo. Please add more coins to your balance.";
                case "You cannot unlock your own photo" ->
                        "You cannot unlock your own photo.";
                case "You have already unlocked this photo" ->
                        "You have already unlocked this photo.";
                case "Photo not found" ->
                        "The requested photo could not be found.";
                case "User not found" ->
                        "Your user account could not be found. Please try logging in again.";
                default ->
                        "An error occurred while unlocking the photo: " + e.getMessage();
            };
            redirectAttributes.addFlashAttribute("error", errorMessage);

            // В случае ошибки, пытаемся получить владельца фото для редиректа
            try {
                Photo photo = photoService.getPhotoEntity(photoId);
                return "redirect:/photos/user/" + photo.getOwner().getUsername() + "/photos";
            } catch (Exception ex) {
                // Если не удалось получить фото, редиректим на главную
                return "redirect:/";
            }
        }
    }
}