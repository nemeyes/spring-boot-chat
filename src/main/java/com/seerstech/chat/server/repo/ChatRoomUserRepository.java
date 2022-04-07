package com.seerstech.chat.server.repo;

import com.seerstech.chat.server.model.ChatRoomUserDao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ChatRoomUserRepository extends MongoRepository<ChatRoomUserDao, String> {

	//@Query("{'user_id' : ?0}")
	List<ChatRoomUserDao> findByUserId(String userId);
	
	@Query("{'room_id' : ?0}")
	List<ChatRoomUserDao> findByRoomId(String roomId);
	
	ChatRoomUserDao findByRoomIdAndUserId(String roomId, String userId);
	Page<ChatRoomUserDao> findByUserIdAndUserJoinedRoom(String userId, Boolean userJoined, Pageable pageable);
	
	
	@Query(value = "{'room_id' : ?0}", count = true)
	Long countByRoomId(String id);
	
	Long deleteByRoomIdAndUserId(String roomId, String userId);
	Long deleteByRoomId(String roomId);
	
	Boolean existsByRoomIdAndUserId(String roomId, String userId);
	Boolean existsByRoomIdAndUserIdAndUserJoinedRoom(String roomId, String userId, Boolean userJoined);
}
