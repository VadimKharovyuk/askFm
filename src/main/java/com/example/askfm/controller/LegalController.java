package com.example.askfm.controller;

import com.example.askfm.enums.ArticleType;
import com.example.askfm.model.Article;
import com.example.askfm.repository.ArticleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/legal")
@RequiredArgsConstructor
public class LegalController {
    private final ArticleRepository articleRepository;



    @GetMapping("/terms")
    public String showTerms(Model model) {
        Article terms = articleRepository.findLatestByType(ArticleType.TERMS_OF_USE)
                .orElseThrow(() -> new EntityNotFoundException("Terms of Use not found"));
        model.addAttribute("article", terms);
        model.addAttribute("previousVersions",
                articleRepository.findByTypeOrderByLastUpdatedDesc(ArticleType.TERMS_OF_USE));
        return "legal/article";
    }

    @GetMapping("/privacy")
    public String showPrivacy(Model model) {
        Article privacy = articleRepository.findLatestByType(ArticleType.PRIVACY_POLICY)
                .orElseThrow(() -> new EntityNotFoundException("Privacy Policy not found"));
        model.addAttribute("article", privacy);
        model.addAttribute("previousVersions",
                articleRepository.findByTypeOrderByLastUpdatedDesc(ArticleType.PRIVACY_POLICY));
        return "legal/article";
    }
}
