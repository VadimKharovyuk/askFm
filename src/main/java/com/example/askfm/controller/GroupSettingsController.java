package com.example.askfm.controller;

import com.example.askfm.dto.GroupViewDTO;
import com.example.askfm.dto.UpdateGroupDTO;
import com.example.askfm.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/groups/{groupId}/settings")
@RequiredArgsConstructor
public class GroupSettingsController {
    private final GroupService groupService;

    @GetMapping
    public String showSettings(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        GroupViewDTO group = groupService.getGroupById(groupId, userDetails.getUsername());
        if (!group.isCanApproveMembers()) {
            throw new AccessDeniedException("No permission to edit group settings");
        }

        model.addAttribute("group", group);
        return "groups/settings";
    }

    @PostMapping("/update")
    public String updateGroup(
            @PathVariable Long groupId,
            @Valid @ModelAttribute UpdateGroupDTO updateDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "groups/settings";
        }

        try {
            groupService.updateGroup(groupId, updateDTO, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Группа успешно обновлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении группы");
            return "redirect:/groups/" + groupId + "/settings";
        }

        return "redirect:/groups/" + groupId;
    }

    @PostMapping("/media")
    public String updateGroupMedia(
            @PathVariable Long groupId,
            @RequestParam(required = false) MultipartFile avatar,
            @RequestParam(required = false) MultipartFile cover,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            groupService.updateGroupMedia(groupId, avatar, cover, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Медиафайлы успешно обновлены");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении медиафайлов");
        }

        return "redirect:/groups/" + groupId + "/settings";
    }
}