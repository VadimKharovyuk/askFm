package com.example.askfm.controller;

import com.example.askfm.enums.ArticleType;
import com.example.askfm.model.Article;
import com.example.askfm.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/articles")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleService articleService;

    @GetMapping
    public String listArticles(Model model) {
        List<Article> articles = articleService.getAllArticles();
        model.addAttribute("articles", articles);
        model.addAttribute("articleTypes", ArticleType.values());  // Добавляем типы статей
        return "admin/articles/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("article", new Article());
        model.addAttribute("articleTypes", Arrays.asList(ArticleType.values()));
        return "admin/articles/form";
    }

    @PostMapping("/create")
    public String createArticle(@ModelAttribute Article article, RedirectAttributes redirectAttributes) {
        try {
            article.setLastUpdated(LocalDateTime.now());
            articleService.saveArticle(article);
            redirectAttributes.addFlashAttribute("successMessage", "Статья успешно создана");
        } catch (Exception e) {
            log.error("Error creating article: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании статьи: " + e.getMessage());
        }
        return "redirect:/admin/articles";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Article article = articleService.getArticleById(id);
        model.addAttribute("article", article);
        model.addAttribute("articleTypes", Arrays.asList(ArticleType.values()));
        return "admin/articles/form";
    }

    @PostMapping("/edit/{id}")
    public String updateArticle(@PathVariable Long id, @ModelAttribute Article article,
                                RedirectAttributes redirectAttributes) {
        try {
            article.setId(id);
            article.setLastUpdated(LocalDateTime.now());
            articleService.saveArticle(article);
            redirectAttributes.addFlashAttribute("successMessage", "Статья успешно обновлена");
        } catch (Exception e) {
            log.error("Error updating article: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении статьи: " + e.getMessage());
        }
        return "redirect:/admin/articles";
    }

    @PostMapping("/delete/{id}")
    public String deleteArticle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            articleService.deleteArticle(id);
            redirectAttributes.addFlashAttribute("successMessage", "Статья успешно удалена");
        } catch (Exception e) {
            log.error("Error deleting article: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении статьи: " + e.getMessage());
        }
        return "redirect:/admin/articles";
    }

    @GetMapping("/preview/{id}")
    public String previewArticle(@PathVariable Long id, Model model) {
        Article article = articleService.getArticleById(id);
        model.addAttribute("article", article);
        return "admin/articles/preview";
    }
}