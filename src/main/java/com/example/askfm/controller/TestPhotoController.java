package com.example.askfm.controller;

import com.example.askfm.dto.TestPhotoDTO;
import com.example.askfm.service.TestPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@RequestMapping("/test")
@Controller
@RequiredArgsConstructor
public class TestPhotoController {
    private final TestPhotoService testPhotoService;

    // Показ формы загрузки
    @GetMapping("/upload-photo")
    public String showUploadForm() {
        return "upload-photo";  // Имя HTML шаблона
    }

    // Обработка загрузки фото
    @PostMapping("/upload-photo")
    public String handlePhotoUpload(@RequestParam("title") String title,
                                    @RequestParam("description") String description,
                                    @RequestParam("file") MultipartFile file,
                                    RedirectAttributes redirectAttributes) {
        try {
            TestPhotoDTO photo = testPhotoService.savePhoto(title, description, file);
            redirectAttributes.addFlashAttribute("success",
                    "Photo uploaded successfully. URL: " + photo.getImageUrl());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to upload photo: " + e.getMessage());
        }
        return "redirect:/upload-photo";
    }

    // Просмотр загруженного фото
    @GetMapping("/photos/{id}")
    public String viewPhoto(@PathVariable Long id, Model model) {
        try {
            TestPhotoDTO photo = testPhotoService.getPhoto(id);
            model.addAttribute("photo", photo);
            return "view-photo";
        } catch (Exception e) {
            model.addAttribute("error", "Photo not found");
            return "error";
        }
    }
}
