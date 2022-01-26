package com.seerstech.chat.server.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.seerstech.chat.server.model.ChatUserDao;

public interface ChatUserRepository extends MongoRepository<ChatUserDao, String> {
	
	@Query("{'user_id' : ?0}")
	ChatUserDao findByUserId(String userId);
	
	@Query("{'user_id': {$in : ?0}}")
	List<ChatUserDao> findByUserIdIn(List<String> userIds);
	
	//@Query("{ 'user_id' : ?0}")
	Boolean existsByUserId(String userId);
	
	void deleteByUserId(String id);
}
