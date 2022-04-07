package com.seerstech.chat.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DownloadUrlConfig {
    
    @Value("${file.download.url}")
    private String downloadUrl;
        
    @Bean(name="downloadUrl")
    public String downloadUrl() {
    	return downloadUrl;
    }
}
