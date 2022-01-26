package com.seerstech.chat.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SeersChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeersChatApplication.class, args);
		System.out.println("hello");
	}

}
