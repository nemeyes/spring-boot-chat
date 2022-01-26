package com.seerstech.chat.server.repo;

import com.seerstech.chat.server.model.ChatParticipantDao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ChatParticipantRepository extends MongoRepository<ChatParticipantDao, String> {

	@Query("{'user_id' : ?0}")
	List<ChatParticipantDao> findByUserId(String id);
	
	@Query("{'room_id' : ?0}")
	List<ChatParticipantDao> findByRoomId(String id);
	
	@Query(value = "{'room_id' : ?0}", count = true)
	Long countByRoomId(String id);
	
	//@Query(value = "{'room_id' : ?0, 'user_id' : ?1}")
	Long deleteByRoomIdAndUserId(String roomId, String userId);
	
	Boolean existsByRoomIdAndUserId(String roomId, String userId);
}
