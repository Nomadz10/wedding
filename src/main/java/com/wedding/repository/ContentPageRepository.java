package com.wedding.repository;

import com.wedding.model.ContentPage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ContentPageRepository extends JpaRepository<ContentPage, Long> {
    Optional<ContentPage> findBySlug(String slug);
    List<ContentPage> findByPublishedTrueOrderByDisplayOrderAscNavLabelAsc();
    List<ContentPage> findAllByOrderByDisplayOrderAscNavLabelAsc();
    boolean existsBySlug(String slug);
}
