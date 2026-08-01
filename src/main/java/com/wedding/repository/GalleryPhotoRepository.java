package com.wedding.repository;

import com.wedding.model.GalleryPhoto;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GalleryPhotoRepository extends JpaRepository<GalleryPhoto, Long> {

    List<GalleryPhoto> findByApprovedTrueOrderByUploadedAtDesc();

    List<GalleryPhoto> findByApprovedTrueOrderByUploadedAtDesc(Limit limit);

    List<GalleryPhoto> findAllByOrderByUploadedAtDesc();

    long countByApprovedTrue();

    /** Featured + approved photos for the home slideshow (newest first). */
    List<GalleryPhoto> findByFeaturedTrueAndApprovedTrueOrderByUploadedAtDesc();

    long countByFeaturedTrueAndApprovedTrue();
}
