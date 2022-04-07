package com.seerstech.chat.server.repo;

import com.seerstech.chat.server.constant.ChatRoleEnum;
import com.seerstech.chat.server.model.ChatRoleDao;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRoleRepository extends MongoRepository<ChatRoleDao, String> {
	
	ChatRoleDao findByRole(ChatRoleEnum role);
	
	boolean existsByRole(ChatRoleEnum role);
}
