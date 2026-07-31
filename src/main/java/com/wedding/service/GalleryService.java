package com.wedding.service;

import com.wedding.model.GalleryPhoto;
import com.wedding.repository.GalleryPhotoRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GalleryService {

    private final GalleryPhotoRepository repo;
    private final FileStorageService fileStorage;

    public GalleryService(GalleryPhotoRepository repo, FileStorageService fileStorage) {
        this.repo = repo;
        this.fileStorage = fileStorage;
    }

    /** Stores each valid image and records it. Returns how many were saved. */
    public int saveAll(MultipartFile[] files, String uploaderName) {
        if (files == null) return 0;
        String name = (uploaderName == null || uploaderName.isBlank()) ? null : uploaderName.trim();
        int saved = 0;
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty() || !isImage(f)) continue;
            String stored = fileStorage.storeImageAsJpeg(f);
            if (stored == null) continue;
            GalleryPhoto p = new GalleryPhoto();
            p.setFilename(stored);
            p.setUploaderName(name);
            p.setUploadedAt(LocalDateTime.now());
            p.setApproved(true);
            repo.save(p);
            saved++;
        }
        return saved;
    }

    public List<GalleryPhoto> approved() {
        return repo.findByApprovedTrueOrderByUploadedAtDesc();
    }

    public List<GalleryPhoto> recent(int max) {
        return repo.findByApprovedTrueOrderByUploadedAtDesc(Limit.of(max));
    }

    public List<GalleryPhoto> all() {
        return repo.findAllByOrderByUploadedAtDesc();
    }

    public long approvedCount() {
        return repo.countByApprovedTrue();
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
