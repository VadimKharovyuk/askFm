package com.example.askfm.controller;

import com.example.askfm.model.User;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;
@RequiredArgsConstructor
@Controller
@RequestMapping("/rewards")
public class UserRewardController {
    private final UserService userService;

    @GetMapping
    public String showRewardPage(Model model, @AuthenticationPrincipal UserDetails principal) {
        String username = principal.getUsername();
        User user = userService.findByUsername(username);

        model.addAttribute("balance", user.getBalance());
        model.addAttribute("rewardAvailable", userService.canClaimDailyReward(username));
        model.addAttribute("canClaimPageVisitReward", userService.canClaimPageVisitReward(username));

        return "balance/rewards";
    }


    @GetMapping("/page-visit")
    public ResponseEntity<?> claimPageVisitReward(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();

        if (!userService.canClaimPageVisitReward(username)) {
            return ResponseEntity
                    .badRequest()
                    .body("You have already claimed your page visit reward today");
        }

        return userService.claimPageVisitReward(username)
                .map(reward -> ResponseEntity.ok(Map.of(
                        "message", "Successfully claimed page visit reward",
                        "reward", reward
                )))
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/api/page-visit")
    @ResponseBody
    public ResponseEntity<?> handleInstagramVisit(@AuthenticationPrincipal UserDetails userDetails) {
        return claimPageVisitReward(userDetails);
    }


    @PostMapping("/claim")
    public String claimReward(RedirectAttributes redirectAttributes,
                              @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        Optional<BigDecimal> reward = userService.claimDailyReward(username);

        if (reward.isPresent()) {
            redirectAttributes.addFlashAttribute("rewardSuccess", "Вы получили " + reward.get() + " монет!");
        } else {
            redirectAttributes.addFlashAttribute("rewardError", "Вы уже получили награду сегодня!");
        }

        return "redirect:/rewards";
    }

}