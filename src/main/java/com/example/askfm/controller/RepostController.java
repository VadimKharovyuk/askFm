package com.example.askfm.controller;

import com.example.askfm.dto.CreateRepostRequest;
import com.example.askfm.dto.RepostDTO;
import com.example.askfm.model.User;
import com.example.askfm.service.RepostService;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Slf4j
@Controller
@RequestMapping("/reposts")
@RequiredArgsConstructor
public class RepostController {
    private final RepostService repostService;
    private final UserService userService;


    @PostMapping("/{postId}/repost")
    public String repost(@PathVariable Long postId,
                         @AuthenticationPrincipal UserDetails userDetails,
                         @ModelAttribute CreateRepostRequest request,
                         RedirectAttributes redirectAttributes) {
        try {
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username);

            request.setPostId(postId);
            RepostDTO repost = repostService.createRepost(user.getId(), request);

            log.info("Repost created: id={}, userId={}, postId={}",
                    repost.getId(), user.getId(), postId);

            redirectAttributes.addFlashAttribute("success", "Post successfully reposted!");
        } catch (Exception e) {
            log.error("Failed to create repost: postId={}, error={}", postId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users/" + userDetails.getUsername();
    }

    @GetMapping("/user/{userId}")
    public String getUserReposts(@PathVariable Long userId, Model model,
                                 @PageableDefault(size = 20) Pageable pageable) {
        model.addAttribute("reposts", repostService.getUserReposts(userId, pageable));
        return "reposts/user-reposts";
    }


    @DeleteMapping("/{repostId}")
    public String deleteRepost(@PathVariable Long repostId,
                               @AuthenticationPrincipal User user,
                               RedirectAttributes redirectAttributes) {
        try {
            repostService.deleteRepost(user.getId(), repostId);
            redirectAttributes.addFlashAttribute("success", "Repost removed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reposts";
    }
}