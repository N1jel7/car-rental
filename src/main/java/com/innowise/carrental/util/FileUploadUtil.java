package com.innowise.carrental.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public class FileUploadUtil {

    private static final Logger log = LoggerFactory.getLogger(FileUploadUtil.class);

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private static final String APP_PROPERTIES = "app.properties";
    private static final String UPLOADS_PATH = "uploads.path";
    private static final String MAX_FILE_SIZE_MB_PROPERTY = "uploads.max-file-size-mb";

    // Loaded once — the single source of truth for the upload size limit,
    // instead of separate hardcoded constants scattered across classes.
    private static final Properties PROPERTIES = PropertiesLoader.load(APP_PROPERTIES);

    private FileUploadUtil() {

    }

    public static String save(InputStream inputStream, String originalFilename,
                              String subfolder, String uploadsRoot) throws IOException {

        String extension = getExtension(originalFilename);
        validateExtension(extension);

        String filename = UUID.randomUUID() + "." + extension;
        String relativePath = subfolder + "/" + filename;

        Path targetDir = Paths.get(uploadsRoot, subfolder);
        Files.createDirectories(targetDir);

        Path targetFile = targetDir.resolve(filename);
        long copied = Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);

        long maxFileSizeBytes = getMaxFileSizeBytes();
        if (copied > maxFileSizeBytes) {
            Files.deleteIfExists(targetFile);
            throw new IOException("File too large: max size is %d MB".formatted(maxFileSizeBytes / (1024 * 1024)));
        }

        log.info("Saved upload: {}", relativePath);
        return relativePath;
    }


    public static void delete(String relativePath, String uploadsRoot) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }

        if (relativePath.contains("..")) {
            log.warn("Rejected suspicious file path: {}", relativePath);
            return;
        }

        try {
            Path file = Paths.get(uploadsRoot, relativePath);
            boolean deleted = Files.deleteIfExists(file);
            if (deleted) {
                log.info("Deleted upload: {}", relativePath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete upload: {}", relativePath, e);
        }
    }


    public static String getUploadsRoot() {
        return PROPERTIES.getProperty(UPLOADS_PATH);
    }

    public static long getMaxFileSizeBytes() {
        return Long.parseLong(PROPERTIES.getProperty(MAX_FILE_SIZE_MB_PROPERTY)) * 1024 * 1024;
    }

    private static String getExtension(String filename) throws IOException {
        if (filename == null || !filename.contains(".")) {
            throw new IOException("File has no extension");
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private static void validateExtension(String extension) throws IOException {
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IOException("File type not allowed: " + extension
                    + ". Allowed: " + ALLOWED_EXTENSIONS);
        }
    }

}
