package com.seerstech.chat.server.repo;

import com.seerstech.chat.server.model.ChatRoleDao;
import com.seerstech.chat.server.model.ChatRoleEnum;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRoleRepository extends MongoRepository<ChatRoleDao, String> {
	
	ChatRoleDao findByRole(ChatRoleEnum role);
	
	boolean existsByRole(ChatRoleEnum role);
}
