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
            boolean isHeic = ext.equals(".heic") || ext.equals(".heif");
            // HEIC needs the dedicated heif-convert; everything else goes via ImageMagick.
            boolean ok = isHeic ? heicToJpeg(tmp, jpg, base) : magickToJpeg(tmp, jpg);
            if (!ok) ok = isHeic ? magickToJpeg(tmp, jpg) : heicToJpeg(tmp, jpg, base);
            if (ok) {
                try { Files.deleteIfExists(tmp); } catch (IOException ignore) { }
                return jpgName;
            }
            try { Files.deleteIfExists(jpg); } catch (IOException ignore) { }
            log.warn("Could not convert upload '{}' to JPEG; keeping original", original);
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

    /** ImageMagick: transcode to JPEG, auto-orient, cap dimensions. */
    private boolean magickToJpeg(Path src, Path out) {
        return run(120, "convert", src.toString() + "[0]",
                "-auto-orient", "-resize", "2000x2000>", "-quality", "85", out.toString())
                && existsNonEmpty(out);
    }

    /** libheif's heif-convert for iPhone HEIC, then ImageMagick to orient/resize. */
    private boolean heicToJpeg(Path src, Path out, String base) {
        Path mid = uploadDir.resolve(base + "-mid.jpg").normalize();
        try {
            if (!run(120, "heif-convert", src.toString(), mid.toString()) || !existsNonEmpty(mid)) {
                return false;
            }
            if (magickToJpeg(mid, out)) return true;
            // ImageMagick step failed — use the full-size heif-convert output as-is.
            Files.move(mid, out, StandardCopyOption.REPLACE_EXISTING);
            return existsNonEmpty(out);
        } catch (IOException e) {
            log.warn("heicToJpeg error: {}", e.toString());
            return false;
        } finally {
            try { Files.deleteIfExists(mid); } catch (IOException ignore) { }
        }
    }

    /** Runs an external command, returns true on exit code 0 within the timeout. */
    private boolean run(int timeoutSeconds, String... cmd) {
        try {
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            boolean finished = p.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) { p.destroyForcibly(); log.warn("Command timed out: {}", cmd[0]); return false; }
            if (p.exitValue() != 0) {
                String out = new String(p.getInputStream().readAllBytes()).strip();
                log.warn("Command {} failed (exit {}): {}", cmd[0], p.exitValue(), out);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("Command {} error: {}", cmd.length > 0 ? cmd[0] : "?", e.toString());
            return false;
        }
    }

    private boolean existsNonEmpty(Path p) {
        try { return Files.exists(p) && Files.size(p) > 0; } catch (IOException e) { return false; }
    }
}
