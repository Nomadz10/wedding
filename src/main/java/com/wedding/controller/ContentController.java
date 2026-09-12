package com.wedding.controller;

import com.wedding.model.ContentPage;
import com.wedding.service.ContentPageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class ContentController {

    private final ContentPageService pages;

    public ContentController(ContentPageService pages) {
        this.pages = pages;
    }

    @GetMapping("/info/{slug}")
    public String page(@PathVariable String slug, Model model) {
        Optional<ContentPage> page = pages.bySlug(slug);
        if (page.isEmpty() || !page.get().isPublished()) {
            return "redirect:/";
        }
        model.addAttribute("page", page.get());
        return "content-page";
    }
}
