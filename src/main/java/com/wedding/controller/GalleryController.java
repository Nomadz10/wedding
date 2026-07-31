package com.wedding.controller;

import com.wedding.service.GalleryService;
import com.wedding.service.QrService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class GalleryController {

    private final GalleryService galleryService;
    private final QrService qrService;

    public GalleryController(GalleryService galleryService, QrService qrService) {
        this.galleryService = galleryService;
        this.qrService = qrService;
    }

    /** Public photo wall — everything guests have shared. */
    @GetMapping("/gallery")
    public String gallery(Model model) {
        model.addAttribute("photos", galleryService.approved());
        model.addAttribute("shareUrl", shareUrl());
        return "gallery";
    }

    /** The mobile-friendly page a guest lands on after scanning the QR code. */
    @GetMapping("/share")
    public String sharePage() {
        return "share";
    }

    /** Receives the guest's selected photos. */
    @PostMapping("/share")
    public String upload(@RequestParam(value = "photos", required = false) MultipartFile[] photos,
                         @RequestParam(required = false) String uploaderName,
                         RedirectAttributes ra) {
        int saved = galleryService.saveAll(photos, uploaderName);
        ra.addFlashAttribute("saved", saved);
        return "redirect:/share";
    }

    /** PNG QR code that points at the /share page (for printing / displaying). */
    @GetMapping(value = "/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] qr(@RequestParam(defaultValue = "480") int size) {
        int s = Math.min(Math.max(size, 120), 1200);
        return qrService.pngFor(shareUrl(), s);
    }

    /** Absolute URL of the /share page, honouring proxy headers in production. */
    private String shareUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/share").toUriString();
    }
}
