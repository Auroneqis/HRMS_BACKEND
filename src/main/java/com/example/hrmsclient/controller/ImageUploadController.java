package com.example.hrmsclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class ImageUploadController {

    // ✅ All values read from application.properties — nothing hardcoded
    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.upload.attendance-dir}")
    private String attendanceUploadDir;

    @Value("${app.upload.profile-dir}")
    private String profileUploadDir;

    @Value("${app.upload.document-dir}")
    private String documentUploadDir;

    @Value("${app.upload.max-file-size}")
    private long maxFileSize;

    @PostMapping("/attendance-photo")
    public ResponseEntity<?> uploadAttendancePhoto(
            @RequestParam("file") MultipartFile file) {

        ResponseEntity<?> validation = validateImage(file);
        if (validation != null) return validation;

        try {
            String fileName = "checkin_" + UUID.randomUUID() + getExtension(file);
            saveFile(file, attendanceUploadDir, fileName);
            String fileUrl = baseUrl + "/" + attendanceUploadDir + fileName;

            return ResponseEntity.ok(Map.of(
                "status",   "success",
                "message",  "Attendance photo uploaded successfully",
                "photoUrl", fileUrl
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status",  "error",
                "message", "Failed to upload photo: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/profile-photo")
    public ResponseEntity<?> uploadProfilePhoto(
            @RequestParam("file") MultipartFile file) {

        ResponseEntity<?> validation = validateImage(file);
        if (validation != null) return validation;

        try {
            String fileName = "profile_" + UUID.randomUUID() + getExtension(file);
            saveFile(file, profileUploadDir, fileName);
            String fileUrl = baseUrl + "/" + profileUploadDir + fileName;

            return ResponseEntity.ok(Map.of(
                "status",   "success",
                "message",  "Profile photo uploaded successfully",
                "photoUrl", fileUrl
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status",  "error",
                "message", "Failed to upload photo: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/document")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error", "message", "Please select a file to upload"
            ));
        }

        if (file.getSize() > maxFileSize) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "File size must be less than " + (maxFileSize / 1024 / 1024) + "MB"
            ));
        }

        try {
            String fileName = "doc_" + UUID.randomUUID() + getExtension(file);
            saveFile(file, documentUploadDir, fileName);
            String fileUrl = baseUrl + "/" + documentUploadDir + fileName;

            return ResponseEntity.ok(Map.of(
                "status",      "success",
                "message",     "Document uploaded successfully",
                "documentUrl", fileUrl
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status",  "error",
                "message", "Failed to upload document: " + e.getMessage()
            ));
        }
    }
    // Validate image file — type and size
    private ResponseEntity<?> validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error", "message", "Please select a photo to upload"
            ));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error", "message", "Only image files are allowed (jpg, png, etc.)"
            ));
        }
        if (file.getSize() > maxFileSize) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "File size must be less than " + (maxFileSize / 1024 / 1024) + "MB"
            ));
        }
        return null; // null = valid
    }


    private void saveFile(MultipartFile file, String dir, String fileName) throws IOException {
        File uploadDir = new File(dir);
        if (!uploadDir.exists()) uploadDir.mkdirs();
        Path filePath = Paths.get(dir + fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
    }
    private String getExtension(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            return original.substring(original.lastIndexOf("."));
        }
        return ".jpg";
    }
}