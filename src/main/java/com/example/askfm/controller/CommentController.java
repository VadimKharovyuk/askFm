package com.example.askfm.controller;

import com.example.askfm.dto.CommentDTO;
import com.example.askfm.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add")
    public String addComment(@RequestParam Long postId,
                             @ModelAttribute("newComment") CommentDTO commentDTO,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to comment");
            return "redirect:/posts/" + postId;
        }

        commentService.createComment(
                postId,
                commentDTO.getContent(),
                userDetails.getUsername()
        );

        return "redirect:/posts/" + postId;
    }

    @PostMapping("/{id}/delete")
    public String deleteComment(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes,
                                @RequestParam Long postId) {
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/posts/" + postId;
        }

        commentService.deleteComment(id, userDetails.getUsername());
        return "redirect:/posts/" + postId;
    }


}
