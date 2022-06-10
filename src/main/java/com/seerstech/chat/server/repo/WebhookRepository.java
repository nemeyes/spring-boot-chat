package com.seerstech.chat.server.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.seerstech.chat.server.model.WebhookDao;

public interface WebhookRepository extends MongoRepository<WebhookDao, String> {
	
}
