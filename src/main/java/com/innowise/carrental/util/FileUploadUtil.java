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
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String APP_PROPERTIES = "app.properties";
    private static final String UPLOADS_PATH = "uploads.path";

    private FileUploadUtil() {

    }

    public static String save(InputStream inputStream, String originalFilename,
                              String subfolder, String uploadsRoot) throws IOException {

        String extension = getExtension(originalFilename);
        validateExtension(extension);

        // Generate a safe unique filename
        String filename = UUID.randomUUID() + "." + extension;
        String relativePath = subfolder + "/" + filename;

        Path targetDir = Paths.get(uploadsRoot, subfolder);
        Files.createDirectories(targetDir);

        Path targetFile = targetDir.resolve(filename);
        long copied = Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);

        if (copied > MAX_FILE_SIZE) {
            Files.deleteIfExists(targetFile);
            throw new IOException("File too large: max size is 10 MB");
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
        try (InputStream in = FileUploadUtil.class.getClassLoader()
                .getResourceAsStream(APP_PROPERTIES)) {
            if (in == null) {
                throw new IllegalStateException("app.properties not found");
            }
            Properties properties = new Properties();
            properties.load(in);
            return properties.getProperty(UPLOADS_PATH);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read uploads.path", e);
        }
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
