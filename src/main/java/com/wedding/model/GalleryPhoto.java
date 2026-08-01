package com.wedding.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

/** A photo uploaded by a guest to the shared photo wall. */
@Entity
@Table(name = "gallery_photos")
public class GalleryPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stored filename, served from /uploads/**. */
    @Column(nullable = false)
    private String filename;

    /** Optional name the guest typed in. */
    private String uploaderName;

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    /** Visible on the public wall when true. Lets the couple hide a photo. */
    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean approved = true;

    /** Shown in the rotating slideshow on the home page (curated by the couple).
     *  ColumnDefault lets this column be added safely to an existing table. */
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean featured = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }
}
