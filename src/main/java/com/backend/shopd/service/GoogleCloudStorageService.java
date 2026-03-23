package com.backend.shopd.service;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import com.backend.shopd.config.GoogleCloudStorageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCloudStorageService {
    
    private final Storage storage;
    private final GoogleCloudStorageConfig gcsConfig;
    
    /**
     * Upload a file to Google Cloud Storage
     * @param file The file to upload
     * @param folder Optional folder path (e.g., "images", "documents")
     * @return The blob name (path) of the uploaded file
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String bucketName = gcsConfig.getBucket().getName();
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String blobName = (folder != null ? folder + "/" : "") + UUID.randomUUID().toString() + extension;
        
        BlobId blobId = BlobId.of(bucketName, blobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
        
        // Upload the file
        storage.create(blobInfo, file.getBytes());
        
        log.info("File uploaded successfully to GCS: {}", blobName);
        return blobName;
    }
    
    /**
     * Upload a file from local path to Google Cloud Storage
     * @param localFilePath The local file path
     * @param folder Optional folder in GCS
     * @return The blob name of the uploaded file
     */
    public String uploadLocalFile(Path localFilePath, String folder) throws IOException {
        String bucketName = gcsConfig.getBucket().getName();
        
        String filename = localFilePath.getFileName().toString();
        String blobName = (folder != null ? folder + "/" : "") + filename;
        
        BlobId blobId = BlobId.of(bucketName, blobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(Files.probeContentType(localFilePath))
                .build();
        
        storage.create(blobInfo, Files.readAllBytes(localFilePath));
        
        log.info("Local file uploaded to GCS: {}", blobName);
        return blobName;
    }
    
    /**
     * Download a file from Google Cloud Storage as bytes
     * @param blobName The name/path of the blob in GCS
     * @return byte array of the file
     */
    public byte[] downloadFile(String blobName) {
        String bucketName = gcsConfig.getBucket().getName();
        Blob blob = storage.get(BlobId.of(bucketName, blobName));
        
        if (blob == null) {
            log.error("File not found in GCS: {}", blobName);
            throw new RuntimeException("File not found: " + blobName);
        }
        
        return blob.getContent();
    }
    
    /**
     * Generate a signed URL for temporary public access to a private file
     * @param blobName The name/path of the blob in GCS
     * @param durationMinutes How long the URL should be valid (in minutes)
     * @return Signed URL as string
     */
    public String generateSignedUrl(String blobName, long durationMinutes) {
        String bucketName = gcsConfig.getBucket().getName();
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, blobName)).build();
        
        return storage.signUrl(
                blobInfo,
                durationMinutes,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature()
        ).toString();
    }
    
    /**
     * Get the public URL for a file (works only if bucket/file is public)
     * @param blobName The name/path of the blob in GCS
     * @return Public URL
     */
    public String getPublicUrl(String blobName) {
        String bucketName = gcsConfig.getBucket().getName();
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, blobName);
    }
    
    /**
     * Delete a file from Google Cloud Storage
     * @param blobName The name/path of the blob to delete
     * @return true if deleted, false if file didn't exist
     */
    public boolean deleteFile(String blobName) {
        String bucketName = gcsConfig.getBucket().getName();
        boolean deleted = storage.delete(BlobId.of(bucketName, blobName));
        
        if (deleted) {
            log.info("File deleted from GCS: {}", blobName);
        } else {
            log.warn("File not found for deletion: {}", blobName);
        }
        
        return deleted;
    }
    
    /**
     * List all files in a folder/prefix
     * @param folder The folder prefix (e.g., "images/")
     * @return List of blob names
     */
    public List<String> listFiles(String folder) {
        String bucketName = gcsConfig.getBucket().getName();
        List<String> files = new ArrayList<>();
        
        Storage.BlobListOption options = folder != null && !folder.isEmpty()
                ? Storage.BlobListOption.prefix(folder)
                : Storage.BlobListOption.prefix("");
        
        Page<Blob> blobs = storage.list(bucketName, options);
        
        for (Blob blob : blobs.iterateAll()) {
            files.add(blob.getName());
        }
        
        return files;
    }
    
    /**
     * Check if a file exists in GCS
     * @param blobName The name/path of the blob
     * @return true if exists
     */
    public boolean fileExists(String blobName) {
        String bucketName = gcsConfig.getBucket().getName();
        Blob blob = storage.get(BlobId.of(bucketName, blobName));
        return blob != null && blob.exists();
    }
    
    /**
     * Copy a file within GCS (can be used for renaming or moving)
     * @param sourceBlobName Source file path
     * @param destinationBlobName Destination file path
     */
    public void copyFile(String sourceBlobName, String destinationBlobName) {
        String bucketName = gcsConfig.getBucket().getName();
        BlobId source = BlobId.of(bucketName, sourceBlobName);
        BlobId target = BlobId.of(bucketName, destinationBlobName);
        
        Storage.CopyRequest request = Storage.CopyRequest.newBuilder()
                .setSource(source)
                .setTarget(target)
                .build();
        
        storage.copy(request);
        log.info("File copied from {} to {}", sourceBlobName, destinationBlobName);
    }
    
    /**
     * Get file metadata
     * @param blobName The name/path of the blob
     * @return BlobInfo with metadata
     */
    public BlobInfo getFileMetadata(String blobName) {
        String bucketName = gcsConfig.getBucket().getName();
        Blob blob = storage.get(BlobId.of(bucketName, blobName));
        
        if (blob == null) {
            throw new RuntimeException("File not found: " + blobName);
        }
        
        return blob;
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}

