package com.recommendation.homestay.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public class UploadUtils {
    private static final Path UPLOAD_DIR;

    static {
        String customDir = System.getProperty("homestay.upload.dir");
        if (customDir == null || customDir.isBlank()) {
            customDir = System.getenv("HOMESTAY_UPLOAD_DIR");
        }
        if (customDir == null || customDir.isBlank()) {
            UPLOAD_DIR = Paths.get(System.getProperty("user.home"), "homestay-uploads");
        } else {
            UPLOAD_DIR = Paths.get(customDir);
        }
    }

    public static Path getUploadDir() {
        return UPLOAD_DIR;
    }
}
