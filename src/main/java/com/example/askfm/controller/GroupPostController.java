package com.example.askfm.controller;

import com.example.askfm.dto.CreateCommentDTO;
import com.example.askfm.dto.GroupPostCommentDTO;
import com.example.askfm.dto.GroupPostDTO;
import com.example.askfm.dto.GroupViewDTO;
import com.example.askfm.enums.MembershipStatus;
import com.example.askfm.service.GroupPostCommentService;
import com.example.askfm.service.GroupPostService;
import com.example.askfm.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/groups/{groupId}/posts")
@RequiredArgsConstructor
@Slf4j
public class GroupPostController {
    private final GroupPostService groupPostService;
    private final GroupService groupService;
    private final GroupPostCommentService commentService;


    @GetMapping("/{postId}")
    public String viewPost(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        try {
            String username = userDetails != null ? userDetails.getUsername() : null;

            GroupViewDTO group = groupService.getGroupById(groupId, username);
            model.addAttribute("group", group);

            if (group.isPrivate() && group.getMembershipStatus() != MembershipStatus.MEMBER) {
                throw new AccessDeniedException("This is a private group");
            }

            GroupPostDTO post = groupPostService.getPostById(postId, username);
            if (!post.getGroupId().equals(groupId)) {
                throw new IllegalArgumentException("Post doesn't belong to this group");
            }

            // Получаем комментарии к посту с пагинацией
            Page<GroupPostCommentDTO> comments = commentService.getPostComments(
                    postId,
                    PageRequest.of(page, GroupPostCommentService.COMMENTS_PER_PAGE)
            );

            model.addAttribute("post", post);
            model.addAttribute("comments", comments);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", comments.getTotalPages());
            model.addAttribute("newComment", new CreateCommentDTO());

            return "groups/post-details";

        } catch (AccessDeniedException e) {
            log.error("Access denied to post {} in group {}: {}", postId, groupId, e.getMessage());
            return "redirect:/groups/" + groupId + "?error=Access denied";
        } catch (Exception e) {
            log.error("Error viewing post {} in group {}: {}", postId, groupId, e.getMessage());
            return "redirect:/groups/" + groupId + "?error=Post not found";
        }
    }

    @PostMapping("/{postId}/like")
    public String likePost(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            groupPostService.likePost(postId, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Лайк успешно поставлен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось поставить лайк.");
        }
        return "redirect:/groups/" + groupId + "/posts/" + postId;
    }


    @PostMapping("/{postId}/comments")
    public String addComment(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @Valid @ModelAttribute("newComment") CreateCommentDTO createCommentDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Комментарий не может быть пустым");
            return "redirect:/groups/" + groupId + "/posts/" + postId;
        }

        try {
            commentService.createComment(postId, userDetails.getUsername(), createCommentDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Комментарий добавлен");
        } catch (Exception e) {
            log.error("Error adding comment to post {} in group {}: {}", postId, groupId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при добавлении комментария");
        }

        return "redirect:/groups/" + groupId + "/posts/" + postId;
    }

    @PostMapping("/{postId}/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            commentService.deleteComment(commentId, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Комментарий удален");
        } catch (Exception e) {
            log.error("Error deleting comment {} from post {}: {}", commentId, postId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении комментария");
        }

        return "redirect:/groups/" + groupId + "/posts/" + postId;
    }




}