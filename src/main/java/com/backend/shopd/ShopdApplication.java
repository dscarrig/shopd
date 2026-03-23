package com.backend.shopd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.backend.shopd.config.FileStorageConfig;
import com.backend.shopd.config.GoogleCloudStorageConfig;

@SpringBootApplication
@EnableConfigurationProperties({FileStorageConfig.class, GoogleCloudStorageConfig.class})
public class ShopdApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopdApplication.class, args);
	}

}
