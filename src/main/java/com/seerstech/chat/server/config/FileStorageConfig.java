package com.seerstech.chat.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
//import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStorageConfig {
	
    @Value("${file.upload.dir}")
    private String uploadDir;
    
    @Bean(name="uploadDir")
    public String uploadDir() {
    	return uploadDir;
    }
}