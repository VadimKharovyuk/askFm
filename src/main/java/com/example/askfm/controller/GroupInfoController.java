package com.example.askfm.controller;

import com.example.askfm.dto.GroupViewDTO;
import com.example.askfm.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/groups/{groupId}")
@RequiredArgsConstructor
public class GroupInfoController {
    private final GroupService groupService;

    @GetMapping("/info")
    public String showGroupInfo(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        GroupViewDTO group = groupService.getGroupById(groupId, username);
        model.addAttribute("group", group);
        return "groups/info";
    }
}