package com.backend.shopd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    /**
     * Upload a multipart file to S3.
     *
     * @param file   the file to upload
     * @param folder optional folder prefix (e.g. "images")
     * @return the S3 key of the uploaded object
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String extension = getFileExtension(file.getOriginalFilename());
        String key = (folder != null ? folder + "/" : "") + UUID.randomUUID() + extension;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        log.info("File uploaded successfully to S3: {}", key);
        return key;
    }

    /**
     * Upload a local file to S3.
     *
     * @param localFilePath path to the local file
     * @param folder        optional folder prefix
     * @return the S3 key of the uploaded object
     */
    public String uploadLocalFile(Path localFilePath, String folder) throws IOException {
        String filename = localFilePath.getFileName().toString();
        String key = (folder != null ? folder + "/" : "") + filename;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(Files.probeContentType(localFilePath))
                .build();

        s3Client.putObject(request, RequestBody.fromFile(localFilePath));
        log.info("Local file uploaded to S3: {}", key);
        return key;
    }

    /**
     * Download a file from S3 as a byte array.
     *
     * @param key the S3 object key
     * @return file contents as bytes
     */
    public byte[] downloadFile(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try {
            return s3Client.getObjectAsBytes(request).asByteArray();
        } catch (NoSuchKeyException e) {
            log.error("File not found in S3: {}", key);
            throw new RuntimeException("File not found: " + key);
        }
    }

    /**
     * Generate a presigned URL for temporary access to a private object.
     *
     * @param key             the S3 object key
     * @param durationMinutes how long the URL should be valid
     * @return presigned URL as a string
     */
    public String generateSignedUrl(String key, long durationMinutes) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(durationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        return presigned.url().toString();
    }

    /**
     * Get the public URL for an object (only valid if the object/bucket is public).
     *
     * @param key the S3 object key
     * @return public URL string
     */
    public String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    /**
     * Delete an object from S3.
     *
     * @param key the S3 object key
     * @return true if deleted, false if the object did not exist
     */
    public boolean deleteFile(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(key).build());
        } catch (NoSuchKeyException e) {
            log.warn("File not found for deletion: {}", key);
            return false;
        }

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
        log.info("File deleted from S3: {}", key);
        return true;
    }

    /**
     * List all object keys under a given folder prefix.
     *
     * @param folder the prefix to filter by (e.g. "images/")
     * @return list of S3 object keys
     */
    public List<String> listFiles(String folder) {
        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(bucketName);

        if (folder != null && !folder.isEmpty()) {
            requestBuilder.prefix(folder);
        }

        ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());
        List<String> files = new ArrayList<>();

        for (S3Object object : response.contents()) {
            files.add(object.key());
        }

        return files;
    }

    /**
     * Check whether an object exists in S3.
     *
     * @param key the S3 object key
     * @return true if the object exists
     */
    public boolean fileExists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(key).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    /**
     * Copy an object within the same bucket.
     *
     * @param sourceKey      source object key
     * @param destinationKey destination object key
     */
    public void copyFile(String sourceKey, String destinationKey) {
        CopyObjectRequest request = CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(sourceKey)
                .destinationBucket(bucketName)
                .destinationKey(destinationKey)
                .build();

        s3Client.copyObject(request);
        log.info("File copied from {} to {}", sourceKey, destinationKey);
    }

    /**
     * Get metadata for an S3 object.
     *
     * @param key the S3 object key
     * @return HeadObjectResponse containing metadata
     */
    public HeadObjectResponse getFileMetadata(String key) {
        try {
            return s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (NoSuchKeyException e) {
            throw new RuntimeException("File not found: " + key);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}