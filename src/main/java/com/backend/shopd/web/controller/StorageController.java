package com.backend.shopd.web.controller;

import com.backend.shopd.service.AwsService;
import com.backend.shopd.service.GoogleCloudStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Slf4j
public class StorageController {
    
    private final AwsService storageService;
    // private final GoogleCloudStorageService storageService;
    
    /**
     * Upload a file to AWS S3
     * POST /api/storage/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "images") String folder) {
        
        try {
            String blobName = storageService.uploadFile(file, folder);
            String publicUrl = storageService.getPublicUrl(blobName);
            
            Map<String, String> response = new HashMap<>();
            response.put("blobName", blobName);
            response.put("publicUrl", publicUrl);
            response.put("message", "File uploaded successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error uploading file", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Generate a signed URL for temporary access
     * GET /api/storage/signed-url/{blobName}
     */
    @GetMapping("/signed-url/{blobName}")
    public ResponseEntity<Map<String, String>> getSignedUrl(
            @PathVariable String blobName,
            @RequestParam(value = "duration", defaultValue = "60") long durationMinutes) {
        
        try {
            String signedUrl = storageService.generateSignedUrl(blobName, durationMinutes);
            
            Map<String, String> response = new HashMap<>();
            response.put("signedUrl", signedUrl);
            response.put("expiresInMinutes", String.valueOf(durationMinutes));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error generating signed URL", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate signed URL: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Download a file from Google Cloud Storage
     * GET /api/storage/download/{blobName}
     */
    @GetMapping("/download/{blobName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String blobName) {
        try {
            byte[] fileContent = storageService.downloadFile(blobName);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + blobName + "\"")
                    .body(fileContent);
                    
        } catch (Exception e) {
            log.error("Error downloading file", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * List all files in a folder
     * GET /api/storage/list
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listFiles(
            @RequestParam(value = "folder", required = false) String folder) {
        
        try {
            List<String> files = storageService.listFiles(folder);
            
            Map<String, Object> response = new HashMap<>();
            response.put("files", files);
            response.put("count", files.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error listing files", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to list files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Delete a file from AWS S3
     * DELETE /api/storage/{blobName}
     */
    @DeleteMapping("/{blobName}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String blobName) {
        try {
            boolean deleted = storageService.deleteFile(blobName);
            
            Map<String, String> response = new HashMap<>();
            if (deleted) {
                response.put("message", "File deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "File not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error deleting file", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Check if a file exists
     * GET /api/storage/exists/{blobName}
     */
    @GetMapping("/exists/{blobName}")
    public ResponseEntity<Map<String, Boolean>> fileExists(@PathVariable String blobName) {
        boolean exists = storageService.fileExists(blobName);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
}
