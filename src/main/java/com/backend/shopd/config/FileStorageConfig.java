package com.backend.shopd.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "file.storage")
@Data
public class FileStorageConfig {
    private String uploadDir = "uploads/images";
}
