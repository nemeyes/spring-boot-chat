package com.seerstech.chat.server.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.seerstech.chat.server.model.ChatRoomDao;

public interface ChatRoomRepository extends MongoRepository<ChatRoomDao, String> {
	
	//@Query("{'room_id' : ?0}")
	ChatRoomDao findByRoomId(String roomId);
	
	Page<ChatRoomDao> findAll(Pageable pageable);

	//@Query("{'room_id' : ?0}")
	Boolean existsByRoomId(String roomId);
	
	Long deleteByRoomId(String roomId);
}
