package com.thong.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_TYPES = List.of(
        "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    // ── Upload image → returns permanent URL ─────────────────────────────
    @SuppressWarnings("unchecked")
    public String uploadProfileImage(MultipartFile file) throws IOException {

        // Validate
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                "Only JPEG, PNG and WebP images are allowed"
            );
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File must not exceed 5MB");
        }

        // Upload to Cloudinary
        Map<String, Object> options = ObjectUtils.asMap(
            "folder",          "hr-payroll/profile-images",
            "transformation",  List.of(ObjectUtils.asMap(
                        "width",  200,
                        "height", 200,
                        "crop",   "fill",      // auto crop to square
                        "gravity","face"       // focus on face if detected
                )),
            "resource_type",   "image"
        );

        Map<String, Object> result = cloudinary.uploader()
            .upload(file.getBytes(), options);

        //  Return the permanent HTTPS URL
        String url = (String) result.get("secure_url");
        log.info("Uploaded profile image to Cloudinary: {}", url);
        return url;
    }

    // ── Delete image by URL ───────────────────────────────────────────────
    public void deleteProfileImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        try {
            // Extract public_id from URL
            // URL format: https://res.cloudinary.com/{cloud}/image/upload/v123/{folder}/{public_id}.jpg
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Deleted Cloudinary image: {}", publicId);
            }
        } catch (Exception e) {
            log.warn("Failed to delete Cloudinary image: {}", imageUrl, e);
            // Non-critical — don't throw
        }
    }

    // Extract public_id from Cloudinary URL
    private String extractPublicId(String url) {
        try {
            // e.g. .../upload/v1234/hr-payroll/profile-images/abc123.jpg
            // public_id = hr-payroll/profile-images/abc123
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) return null;

            String afterUpload = url.substring(uploadIndex + 8); // skip "/upload/"

            // Skip version segment if present (v1234567/)
            if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
            }

            // Remove file extension
            int dotIndex = afterUpload.lastIndexOf(".");
            return dotIndex != -1 ? afterUpload.substring(0, dotIndex) : afterUpload;

        } catch (Exception e) {
            return null;
        }
    }
}