package com.wedding.config;

import com.wedding.service.GalleryService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** On startup, fills in the "date taken" for any photos uploaded before that was tracked. */
@Component
public class GalleryBackfillRunner implements ApplicationRunner {

    private final GalleryService galleryService;

    public GalleryBackfillRunner(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            galleryService.backfillTakenAt();
        } catch (Exception e) {
            // Never let a backfill hiccup stop the app from starting.
        }
    }
}
