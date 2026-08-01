package com.wedding.controller;

import com.wedding.service.EventService;
import com.wedding.service.GalleryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final EventService eventService;
    private final GalleryService galleryService;

    public HomeController(EventService eventService, GalleryService galleryService) {
        this.eventService = eventService;
        this.galleryService = galleryService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("days", eventService.days());
        // Home shows only the curated "featured" photos, as a rotating slideshow.
        model.addAttribute("galleryPhotos", galleryService.featured());
        return "index";
    }

    @GetMapping("/events")
    public String events(Model model) {
        model.addAttribute("days", eventService.days());
        model.addAttribute("events", eventService.timeline());
        return "events";
    }
}
