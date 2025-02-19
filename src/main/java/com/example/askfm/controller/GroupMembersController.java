package com.example.askfm.controller;

import com.example.askfm.dto.GroupMemberDTO;
import com.example.askfm.dto.GroupMembersListDTO;
import com.example.askfm.model.Group;
import com.example.askfm.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/groups/{groupId}/members")
@RequiredArgsConstructor
public class GroupMembersController {
    private final GroupService groupService;
    private static final int PAGE_SIZE = 20;


    @GetMapping
    public String listMembers(
            @PathVariable Long groupId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        // Получаем информацию о группе и участниках
        GroupMembersListDTO groupMembersList = groupService.getGroupMembers(groupId, search, PageRequest.of(page, PAGE_SIZE));

        // Добавляем данные в модель
        model.addAttribute("group", groupMembersList); // Вся информация о группе и участниках
        model.addAttribute("searchQuery", search);

        return "groups/members";
    }
}