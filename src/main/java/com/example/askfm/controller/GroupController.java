package com.example.askfm.controller;

import com.example.askfm.dto.CreateGroupDTO;
import com.example.askfm.dto.GroupListDTO;
import com.example.askfm.dto.GroupMembershipDTO;
import com.example.askfm.dto.GroupViewDTO;
import com.example.askfm.model.Group;
import com.example.askfm.model.GroupJoinRequest;
import com.example.askfm.model.User;
import com.example.askfm.service.GroupService;
import com.example.askfm.enums.GroupCategory;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
    private static final int PAGE_SIZE = 25;
    @GetMapping
    public String listGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        Sort.Direction dir = Sort.Direction.fromString(direction.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(dir, sort));

        String username = userDetails != null ? userDetails.getUsername() : null;
        Page<GroupListDTO> groups = groupService.getAllGroups(pageRequest, username);

        model.addAttribute("groups", groups);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", groups.getTotalPages());
        model.addAttribute("activeTab", "all");

        return "groups/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("groupDTO", new CreateGroupDTO());
        model.addAttribute("categories", GroupCategory.values());
        return "groups/create";
    }

    @PostMapping("/create")
    public String createGroup(
            @Valid @ModelAttribute("groupDTO") CreateGroupDTO groupDTO,
            BindingResult bindingResult,
            @RequestParam("coverFile") MultipartFile cover,
            @RequestParam("avatarFile") MultipartFile avatar,
            @AuthenticationPrincipal UserDetails user,
            Model model
    ) throws IOException {
        // Получаем username из UserDetails
        String username = user.getUsername();

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", GroupCategory.values());
            return "groups/create";
        }

        if (!cover.isEmpty()) {
            groupDTO.setCover(cover.getBytes());
        }
        if (!avatar.isEmpty()) {
            groupDTO.setAvatar(avatar.getBytes());
        }

        Group group = groupService.createGroup(groupDTO, username);
        return "redirect:/groups/" + group.getId();
    }


    @GetMapping("/{id}")
    public String viewGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        GroupViewDTO group = groupService.getGroupById(id, username);
        model.addAttribute("group", group);
        return "groups/view";
    }

    @PostMapping("/{id}/join")
    public String joinGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            groupService.joinGroup(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ваша заявка на вступление отправлена");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/groups/" + id;
    }

    @PostMapping("/requests/{requestId}/approve")
    public String approveRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            groupService.approveJoinRequest(requestId, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Заявка одобрена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/groups/requests";
    }

    @PostMapping("/requests/{requestId}/reject")
    public String rejectRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            groupService.rejectJoinRequest(requestId, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Заявка отклонена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/groups/requests";
    }

    @PostMapping("/{id}/leave")
    public String leaveGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            groupService.leaveGroup(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Вы покинули группу");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/groups/" + id;
    }

    @GetMapping("/{id}/requests")
    public String viewRequests(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        List<GroupJoinRequest> requests = groupService.getPendingRequests(id, userDetails.getUsername());
        model.addAttribute("requests", requests);
        model.addAttribute("groupId", id);
        return "groups/requests";
    }




    @GetMapping("/my")
    public String getMyGroups(
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<GroupListDTO> groups = groupService.getMyGroups(userDetails.getUsername(), pageRequest);

        model.addAttribute("groups", groups);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", groups.getTotalPages());
        model.addAttribute("activeTab", "my");

        return "groups/list";
    }

    @GetMapping("/managed")
    public String getManagedGroups(
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<GroupListDTO> groups = groupService.getManagedGroups(userDetails.getUsername(), pageRequest);

        model.addAttribute("groups", groups);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", groups.getTotalPages());
        model.addAttribute("activeTab", "managed");

        return "groups/list";
    }
}