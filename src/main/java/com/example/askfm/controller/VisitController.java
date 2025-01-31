package com.example.askfm.controller;

import com.example.askfm.dto.VisitDTO;
import com.example.askfm.model.User;
import com.example.askfm.service.ImageService;
import com.example.askfm.service.UserService;
import com.example.askfm.service.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/visits")
@RequiredArgsConstructor
public class VisitController {
    private final VisitService visitService;
    private final UserService userService;
    private final ImageService imageService;

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    @GetMapping("/my")
    public String getMyVisitors(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {


        // Валидация параметров пагинации
        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        String username = userDetails.getUsername();
        User currentUser = userService.findByUsername(username);

        if (currentUser == null) {
            return "redirect:/login";
        }

        Page<VisitDTO> visitors = visitService.getRecentVisitors(currentUser, page, size);
        Long uniqueVisitors = visitService.getUniqueVisitorsCount(currentUser);
        model.addAttribute("avatarBase64", imageService.getBase64Avatar(currentUser.getAvatar()));
        model.addAttribute("visitors", visitors);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", visitors.getTotalPages());
        model.addAttribute("uniqueVisitors", uniqueVisitors);
        model.addAttribute("username", username);


        return "visits/visitors";
    }
}
