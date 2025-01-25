package com.example.askfm.controller;

import com.example.askfm.dto.PostCreateDTO;
import com.example.askfm.dto.PostDTO;
import com.example.askfm.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;


@PostMapping("/{postId}/like")
public String likePost(@PathVariable Long postId,
                       @AuthenticationPrincipal UserDetails userDetails,
                       HttpServletRequest request) {
    postService.likePost(postId, userDetails.getUsername());

    // Получаем URL предыдущей страницы из заголовка Referer
    String referer = request.getHeader("Referer");

    // Перенаправляем обратно на предыдущую страницу
    return "redirect:" + (referer != null ? referer : "/posts");
}

    @GetMapping("/{postId}")
    public String getPostDetails(@PathVariable Long postId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        String currentUsername = userDetails != null ? userDetails.getUsername() : null;
        postService.incrementViews(postId, currentUsername);

        long postViews = postService.getPostViews(postId);
        model.addAttribute("postViews", postViews);

        PostDTO post = postService.getPostDTO(postService.getPost(postId), currentUsername);
        model.addAttribute("post", post);
        return "posts/post-details";
    }





    @GetMapping
    public String getAllPosts(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String currentUsername = userDetails != null ? userDetails.getUsername() : null;
        model.addAttribute("posts", postService.getUserPosts(currentUsername, currentUsername));
        return "posts/list";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute @Valid PostCreateDTO postForm,
                             BindingResult result,
                             @AuthenticationPrincipal UserDetails userDetails) {
        if (result.hasErrors()) {
            return "posts/create";
        }
        postService.createPost(userDetails.getUsername(), postForm);
        return "redirect:/home";
    }


    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id , @AuthenticationPrincipal UserDetails userDetails) {
        postService.deletePostById(id);
        return "redirect:/users/" + userDetails.getUsername();
    }


    @GetMapping("/{username}/posts")
    public String getUserPosts(@PathVariable String username,
                               Model model,
                               @AuthenticationPrincipal UserDetails userDetails) {
        String currentUsername = userDetails != null ? userDetails.getUsername() : null;
        model.addAttribute("posts", postService.getUserPosts(username, currentUsername));
        return "posts/user-posts";
    }
}