package com.seerstech.chat.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccessTokenExpTimeConfig {
	
    @Value("${token.access.expiration.time}")
    private long accessTokenExpTime;
        
    @Bean(name="accessTokenExpTime")
    public long accessTokenExpTime() {
    	return accessTokenExpTime;
    }
}
