package com.backend.shopd.web.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.shopd.data.entity.ShopdItem;
import com.backend.shopd.service.GoogleCloudStorageService;
import com.backend.shopd.service.ShopdItemService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/items")
public class ShopdItemApiController {
    private static final Logger logger = LoggerFactory.getLogger(ShopdItemApiController.class);
    private final ShopdItemService shopdItemService;
    private final GoogleCloudStorageService gcsService;

    public ShopdItemApiController(
            ShopdItemService shopdItemService,
            GoogleCloudStorageService gcsService) {
        this.shopdItemService = shopdItemService;
        this.gcsService = gcsService;
    }

    @GetMapping
    public List<ShopdItem> getAllItems(){
        return shopdItemService.getAllItems();
    }

    @GetMapping("/get-all-items-from-user/{user_id}")
    public List<ShopdItem> getAllItemsFromUser(@PathVariable UUID user_id){
        return shopdItemService.getItemsByUserId(user_id);
    }

    @GetMapping("/search-by-category")
    public List<ShopdItem> getItemsByCategory(@RequestParam String[] categories){
        return shopdItemService.getItemsByCategory(categories);
    }

    @GetMapping("/{id}")
    public ShopdItem getItemById(@PathVariable UUID id){
        return shopdItemService.getItemById(id);
    }

    @GetMapping("/user-id/{id}")
    public UUID getUserIdByItemId(@PathVariable UUID id){
        return shopdItemService.getUserIdByItemId(id);
    }

    @PostMapping("/create-item/{user_id}")
    public ShopdItem createItem(@PathVariable UUID user_id, @RequestBody ShopdItem item){
        logger.info("Creating item '{}' for user: {}", item.getName(), user_id);
        ShopdItem createdItem = shopdItemService.createItem(item);
        logger.info("Item created successfully with ID: {}", createdItem.getId());
        return createdItem;
    }

    @PutMapping("update-item/{id}")
    public ShopdItem updateItem(@PathVariable UUID id, @RequestBody ShopdItem item){
        return shopdItemService.updateItem(id, item);
    }

    @DeleteMapping("/{user_id}/{id}")
    public void deleteItem(@PathVariable UUID user_id, @PathVariable UUID id){
        shopdItemService.deleteItem(id);
    }

    /**
     * Upload an image for an item
     * @param id The item ID
     * @param file The image file
     * @return Response with the image URL
     */
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<Map<String, String>> uploadItemImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        try {
            logger.info("Received file: {} for item ID: {}", file.getOriginalFilename(), id);
            
            // Verify item exists
            ShopdItem item = shopdItemService.getItemById(id);
            if (item == null) {
                logger.error("Item not found with ID: {}", id);
                Map<String, String> error = new HashMap<>();
                error.put("error", "Item not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Upload to Google Cloud Storage
            String blobName = gcsService.uploadFile(file, "items");
            String imageUrl = gcsService.getPublicUrl(blobName);
            
            // Update item with image URL
            item.setImageUrl(imageUrl);
            shopdItemService.updateItem(id, item);
            
            logger.info("Image uploaded successfully: {}", blobName);
            
            Map<String, String> response = new HashMap<>();
            response.put("blobName", blobName);
            response.put("imageUrl", imageUrl);
            response.put("message", "Image uploaded successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            logger.error("Error uploading image for item: {}", id, ex);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload image: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Serve/download an image file
     * @param filename The filename to serve
     * @param request The HTTP request
     * @return The image file as bytes
     */
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable String filename,
            HttpServletRequest request) {
        try {
            logger.info("Downloading file: {}", filename);
            
            // Construct the blob name (assuming files are in "items" folder)
            String blobName = "items/" + filename;
            
            // Download from Google Cloud Storage
            byte[] fileContent = gcsService.downloadFile(blobName);
            
            // Determine content type
            String contentType = request.getServletContext().getMimeType(filename);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(fileContent);
                    
        } catch (Exception ex) {
            logger.error("Failed to serve file: {}", filename, ex);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Extract filename from a full URL
     */
    private String extractFilenameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
            return url.substring(lastSlashIndex + 1);
        }
        return url;
    }
}
