package com.seerstech.chat.server.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.seerstech.chat.server.model.ChatRoomDao;

public interface ChatRoomRepository extends MongoRepository<ChatRoomDao, String> {
	
	@Query("{'room_id' : ?0}")
	ChatRoomDao findByRoomId(String roomId);

	//@Query("{'room_id' : ?0}")
	Boolean existsByRoomId(String roomId);
}
