package com.wedding.service;

import com.wedding.model.ContentPage;
import com.wedding.repository.ContentPageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContentPageService {

    private final ContentPageRepository repo;

    public ContentPageService(ContentPageRepository repo) {
        this.repo = repo;
    }

    /** Published pages for the public nav / viewing. */
    public List<ContentPage> published() {
        return repo.findByPublishedTrueOrderByDisplayOrderAscNavLabelAsc();
    }

    public List<ContentPage> all() {
        return repo.findAllByOrderByDisplayOrderAscNavLabelAsc();
    }

    public Optional<ContentPage> bySlug(String slug) {
        return slug == null ? Optional.empty() : repo.findBySlug(slug.trim().toLowerCase());
    }

    public Optional<ContentPage> byId(Long id) {
        return repo.findById(id);
    }

    public ContentPage save(ContentPage page) {
        return repo.save(page);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
