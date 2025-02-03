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

    @PostMapping("/delete")
    public String deleteRepost(@RequestParam("postId") Long postId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        log.debug("Attempting to delete repost for postId: {} by user: {}", postId, userDetails.getUsername());

        try {
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username);
            log.debug("Found user with id: {}", user.getId());

            repostService.deleteRepost(user.getId(), postId);
            log.debug("Successfully deleted repost");
            redirectAttributes.addFlashAttribute("success", "Repost removed successfully");
        } catch (Exception e) {
            log.error("Error deleting repost: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users/" + userDetails.getUsername();
    }

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



}