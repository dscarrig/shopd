package com.backend.shopd.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "gcs")
@Data
public class GoogleCloudStorageConfig {
    private Bucket bucket = new Bucket();
    
    @Data
    public static class Bucket {
        private String name = "shopd-uploads";
    }
}
