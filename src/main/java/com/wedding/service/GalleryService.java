package com.wedding.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.wedding.model.GalleryPhoto;
import com.wedding.repository.GalleryPhotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
public class GalleryService {

    private static final Logger log = LoggerFactory.getLogger(GalleryService.class);

    /** Oldest-taken first (chronological); photos without EXIF fall back to their upload time. */
    private static final Comparator<GalleryPhoto> BY_DATE_ASC =
            Comparator.comparing(GalleryPhoto::effectiveDate);

    private final GalleryPhotoRepository repo;
    private final FileStorageService fileStorage;

    public GalleryService(GalleryPhotoRepository repo, FileStorageService fileStorage) {
        this.repo = repo;
        this.fileStorage = fileStorage;
    }

    /** Guest uploads (photo wall only). Returns how many were saved. */
    public int saveAll(MultipartFile[] files, String uploaderName) {
        return saveAll(files, uploaderName, false);
    }

    /** Stores each valid image and records it. `featured` also puts it on the home slideshow. */
    public int saveAll(MultipartFile[] files, String uploaderName, boolean featured) {
        if (files == null) return 0;
        String name = (uploaderName == null || uploaderName.isBlank()) ? null : uploaderName.trim();
        int saved = 0;
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty() || !isImage(f)) continue;
            LocalDateTime takenAt = readTakenAt(f);   // read EXIF before conversion
            String stored = fileStorage.storeImageAsJpeg(f);
            if (stored == null) continue;
            GalleryPhoto p = new GalleryPhoto();
            p.setFilename(stored);
            p.setUploaderName(name);
            p.setUploadedAt(LocalDateTime.now());
            p.setTakenAt(takenAt);
            p.setApproved(true);
            p.setFeatured(featured);
            repo.save(p);
            saved++;
        }
        return saved;
    }

    public List<GalleryPhoto> approved() {
        return repo.findByApprovedTrueOrderByUploadedAtDesc().stream().sorted(BY_DATE_ASC).toList();
    }

    public List<GalleryPhoto> recent(int max) {
        return featured().stream().limit(max).toList();
    }

    /** Curated photos for the home slideshow. */
    public List<GalleryPhoto> featured() {
        return repo.findByFeaturedTrueAndApprovedTrueOrderByUploadedAtDesc().stream().sorted(BY_DATE_ASC).toList();
    }

    public List<GalleryPhoto> all() {
        return repo.findAllByOrderByUploadedAtDesc().stream().sorted(BY_DATE_ASC).toList();
    }

    /**
     * One-time backfill: for photos with no "date taken" yet, read the EXIF from
     * the stored file. Runs on startup; cheap and idempotent (only touches nulls).
     */
    public int backfillTakenAt() {
        int updated = 0;
        for (GalleryPhoto p : repo.findAll()) {
            if (p.getTakenAt() != null) continue;
            Path file = fileStorage.getUploadDir().resolve(p.getFilename());
            if (!Files.exists(file)) continue;
            try (InputStream in = Files.newInputStream(file)) {
                LocalDateTime t = extractTakenAt(in);
                if (t != null) { p.setTakenAt(t); repo.save(p); updated++; }
            } catch (Exception e) {
                log.debug("Backfill EXIF failed for {}: {}", p.getFilename(), e.toString());
            }
        }
        if (updated > 0) log.info("Backfilled 'date taken' for {} photo(s)", updated);
        return updated;
    }

    /** Reads the EXIF "date taken" from an uploaded photo (JPEG/HEIC), or null. */
    private LocalDateTime readTakenAt(MultipartFile f) {
        try (InputStream in = f.getInputStream()) {
            return extractTakenAt(in);
        } catch (Exception e) {
            log.debug("No EXIF date for upload {}: {}", f.getOriginalFilename(), e.toString());
            return null;
        }
    }

    private LocalDateTime extractTakenAt(InputStream in) throws Exception {
        Metadata md = ImageMetadataReader.readMetadata(in);
        ExifSubIFDDirectory dir = md.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (dir != null) {
            Date d = dir.getDateOriginal();
            if (d != null) return LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
        }
        return null;
    }

    public long approvedCount() {
        return repo.countByApprovedTrue();
    }

    public long featuredCount() {
        return repo.countByFeaturedTrueAndApprovedTrue();
    }

    /** Flip whether a photo appears in the home slideshow. */
    public void toggleFeatured(Long id) {
        repo.findById(id).ifPresent(p -> {
            p.setFeatured(!p.isFeatured());
            repo.save(p);
        });
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private boolean isImage(MultipartFile f) {
        String ct = f.getContentType();
        if (ct != null && ct.toLowerCase().startsWith("image/")) return true;
        // fall back to extension check if the browser didn't send a type
        String n = f.getOriginalFilename();
        if (n == null) return false;
        n = n.toLowerCase();
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png")
                || n.endsWith(".gif") || n.endsWith(".webp") || n.endsWith(".heic");
    }
}
