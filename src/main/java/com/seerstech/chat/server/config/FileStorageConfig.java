package com.seerstech.chat.server.config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
//import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import redis.embedded.RedisServer;

@Configuration
public class FileStorageConfig {
	
    @Value("${file.upload.dir}")
    private String uploadDir;
    
    /*
    @PostConstruct
    public void startServer() {
    	System.out.println("start");
    }

    @PreDestroy
    public void stopServer() {
    	System.out.println("stop");
    }
    */
    
    
    @Bean(name="uploadDir")
    public String uploadDir() {
    	return uploadDir;
    }
}