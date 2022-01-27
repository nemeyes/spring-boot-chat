package com.seerstech.chat.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.seerstech.chat.server.config.FileStorageConfig;

@SpringBootApplication
/*
@EnableConfigurationProperties({
		FileStorageConfig.class
})
*/
public class SeersChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeersChatApplication.class, args);
		System.out.println("hello");
	}

}
