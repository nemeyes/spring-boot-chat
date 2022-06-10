package com.seerstech.chat.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Document(collection = "Webhook")
public class WebhookDao {
	
	@Id
	private String id;
	
	@Field("base_url")
	private String baseUrl;
	
	@Field("path")
	private String path;
}