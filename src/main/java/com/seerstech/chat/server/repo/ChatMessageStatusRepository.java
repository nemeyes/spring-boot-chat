package com.seerstech.chat.server.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.seerstech.chat.server.model.ChatMessageStatusDao;
import com.seerstech.chat.server.model.ChatRoomUserDao;

public interface ChatMessageStatusRepository extends MongoRepository<ChatMessageStatusDao, String> {
	
	//@Query("{'user_id' : ?0}")
	List<ChatMessageStatusDao> findByUserId(String userId);
	
	//@Query("{'room_id' : ?0}")
	List<ChatMessageStatusDao> findByRoomId(String roomId);
	
	//@Query("{'message_id' : ?0}")
	List<ChatMessageStatusDao> findByMessageId(String messageId);
	
	//@Query("{'room_id' : ?0, 'user_id' : ?0}")
	List<ChatMessageStatusDao> findByRoomIdAndUserId(String roomId, String userId);
	
	//@Query("{'room_id' : ?0, 'message_id' : ?0}")
	List<ChatMessageStatusDao> findByRoomIdAndMessageId(String roomId, String userId);
	
	//@Query("{'room_id' : ?0, 'user_id' : ?0, 'message_id' : ?0}")
	List<ChatMessageStatusDao> findByRoomIdAndUserIdAndMessageId(String roomId, String userId, String messageId);
	
	Long countByRoomIdAndUserId(String roomId, String userId);
	
	boolean existsByMessageId(String messageId);
	boolean existsByUserIdAndMessageId(String userId, String messageId);
	boolean existsByRoomIdAndUserIdAndMessageId(String roomId, String userId, String messageId);
	
	Long deleteByMessageId(String messageId);
	Long deleteByUserIdAndMessageId(String userId, String messageId);
	Long deleteByRoomIdAndUserIdAndMessageId(String roomId, String userId, String messageId);
	
	Long deleteByRoomIdAndUserId(String roomId, String userId);
	Long deleteByRoomId(String roomId);
	
    @Aggregation(pipeline = { "{ '$group': { '_id' : '$message_id' } }" })
    List<String> findDistinctMessageIdsByRoomIdAndUserId(String roomId, String userId);
    
    List<String> findDistinctMessageIdByRoomIdAndUserId(String roomId, String userId);
	
}
