package com.wedding.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Stores uploaded photos on disk under wedding.upload.dir. */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final Path uploadDir;
    /** Skip the external convert step on Windows (no ImageMagick; convert.exe is unrelated). */
    private final boolean canConvert =
            !System.getProperty("os.name", "").toLowerCase().contains("win");

    public FileStorageService(@Value("${wedding.upload.dir}") String dir) {
        this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create upload dir: " + uploadDir, e);
        }
    }

    public Path getUploadDir() {
        return uploadDir;
    }

    /** Saves the file with a random name, returns the stored filename (or null if empty). */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot).toLowerCase();
        String filename = UUID.randomUUID() + ext;
        try {
            Path target = uploadDir.resolve(filename).normalize();
            if (!target.startsWith(uploadDir)) {
                throw new IllegalArgumentException("Invalid path");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }
    }

    /**
     * Stores an uploaded image, converting it to a web-friendly JPEG via ImageMagick
     * (also auto-orients rotated phone photos and caps huge ones). Browsers can't
     * display iPhone HEIC/HEIF, so this is important. Falls back to storing the
     * original bytes untouched if conversion isn't available or fails.
     */
    public String storeImageAsJpeg(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot).toLowerCase();

        String base = UUID.randomUUID().toString();
        Path tmp = uploadDir.resolve(base + "-src" + (ext.isEmpty() ? ".img" : ext)).normalize();
        try {
            Files.copy(file.getInputStream(), tmp, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store upload", e);
        }

        if (canConvert) {
            String jpgName = base + ".jpg";
            Path jpg = uploadDir.resolve(jpgName).normalize();
            try {
                Process p = new ProcessBuilder(
                        "convert", tmp.toString() + "[0]",
                        "-auto-orient", "-resize", "2000x2000>", "-quality", "85",
                        jpg.toString())
                        .redirectErrorStream(true)
                        .start();
                boolean finished = p.waitFor(90, TimeUnit.SECONDS);
                if (finished && p.exitValue() == 0 && Files.exists(jpg) && Files.size(jpg) > 0) {
                    Files.deleteIfExists(tmp);
                    return jpgName;
                }
                log.warn("Image conversion failed (exit={}), keeping original", finished ? p.exitValue() : "timeout");
                Files.deleteIfExists(jpg);
            } catch (Exception e) {
                log.warn("Image conversion error, keeping original: {}", e.toString());
            }
        }

        // Fallback: keep the original file under a clean stored name.
        String fallbackName = base + (ext.isEmpty() ? ".img" : ext);
        try {
            Files.move(tmp, uploadDir.resolve(fallbackName).normalize(), StandardCopyOption.REPLACE_EXISTING);
            return fallbackName;
        } catch (IOException e) {
            try { Files.deleteIfExists(tmp); } catch (IOException ignore) { /* best effort */ }
            throw new UncheckedIOException("Failed to store upload", e);
        }
    }
}
