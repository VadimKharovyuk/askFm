package com.example.askfm.controller;

import com.example.askfm.dto.*;
import com.example.askfm.enums.ReportCategory;
import com.example.askfm.model.Post;
import com.example.askfm.model.User;
import com.example.askfm.service.CommentService;
import com.example.askfm.service.PostService;
import com.example.askfm.service.RepostService;
import com.example.askfm.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {


    private final PostService postService;
    private final CommentService commentService;
    private final RepostService repostService;
    private final UserService userService;

    @GetMapping("/{postId}")
    public String getPostDetails(@PathVariable Long postId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 @PageableDefault(size = 10) Pageable pageable,
                                 Model model) {
        String currentUsername = userDetails != null ? userDetails.getUsername() : null;

        // Увеличиваем счетчик просмотров
        postService.incrementViews(postId, currentUsername);

        // Получаем детальную информацию о посте
        PostDTO post = postService.getPostDetails(postId, currentUsername);
        model.addAttribute("post", post);

        // Добавляем количество просмотров
        model.addAttribute("postViews", postService.getPostViews(postId));
        model.addAttribute("currentUser", currentUsername);

        // Добавляем форму для репорта
        model.addAttribute("reportForm", new PostReportCreateDTO());
        model.addAttribute("categories", ReportCategory.values());

        // Загружаем комментарии
        Page<ListCommentDTO> comments = commentService.getPostCommentsList(postId, pageable);
        model.addAttribute("comments", comments);

        // Формы для комментария и репоста
        model.addAttribute("newComment", new CommentDTO());
        model.addAttribute("repostForm", new CreateRepostRequest());

        return "posts/post-details";
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
            repostService.createRepost(user.getId(), request);
            redirectAttributes.addFlashAttribute("success", "Post successfully reposted!");
        } catch (Exception e) {
            log.error("Repost error: ", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/posts/" + postId;
    }

    @GetMapping("/search")
    public String searchPosts(@RequestParam(required = false) String tags,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model,
                              @AuthenticationPrincipal UserDetails userDetails) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        String username = userDetails != null ? userDetails.getUsername() : null;

        // Если теги не указаны, можно показать все посты или пустую страницу
        Page<PostDTO> posts;
        if (tags == null || tags.trim().isEmpty()) {
            model.addAttribute("message", "Please enter tags to search");
            posts = Page.empty(pageable);
        } else {
            posts = postService.findPostsByTags(tags, username, pageable);
        }


        model.addAttribute("posts", posts);
        model.addAttribute("tags", tags);
        model.addAttribute("currentPage", page);

        return "posts/by-tag";
    }


    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        log.info("Получен запрос на удаление поста с ID: {}", id);

        Post post = postService.getPost(id);
        log.info("Найден пост с ID: {}, автор: {}", id, post.getAuthor().getUsername());

        if (!post.getAuthor().getUsername().equals(userDetails.getUsername())) {
            log.warn("Отказано в доступе для пользователя: {}", userDetails.getUsername());
            throw new AccessDeniedException("Not authorized");
        }

        postService.deletePost(id);
        log.info("Пост успешно удален");

        return "redirect:/users/" + userDetails.getUsername();
    }



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
        // Преобразование строки тегов в Set
        if (postForm.getTags() != null) {
            Set<String> tagSet = Arrays.stream(postForm.getTags().split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toSet());
            postForm.setTags(tagSet.toString());
        }
        postService.createPost(userDetails.getUsername(), postForm);
        return "redirect:/home";
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
