package com.seerstech.chat.server.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.seerstech.chat.server.constant.ChatMessageEnum;
import com.seerstech.chat.server.constant.ChatNotificationEnum;
import com.seerstech.chat.server.model.ChatMessageDao;

public interface ChatMessageRepository extends MongoRepository<ChatMessageDao, String> {
	
	//@Query("{'room_id' : ?0}")
	Page<ChatMessageDao> findByRoomId(String roomId, Pageable pageable);
	
	//@Query("{'message_id' : ?0}")
	ChatMessageDao findByMessageId(String messageId);
	
	List<ChatMessageDao> findByParentMessageId(String messageId);
	
	List<ChatMessageDao> findByRoomIdAndType(String roomId, ChatMessageEnum type);
	
	//List<ChatMessageDao> findByRoomIdAndTypeAndDeletedTimeGreaterThanCreatedTime(String roomId, ChatMessageEnum type);
	//List<ChatMessageDao> findByRoomIdAndTypeAndCreatedTimeGreaterThanDeletedTime(String roomId, ChatMessageEnum type);
	List<ChatMessageDao> findByRoomIdAndTypeAndDeletedTime(String roomId, ChatMessageEnum type, Long deletedTime);
	
	
	//@Query(value = "{location :{ $nearSphere :{$geometry : {type : \"Point\", coordinates : [?1, ?0] },  $maxDistance :?2}}")
	//List<User> search(float latitude, float longitude, float radius, Pageable page)
	//@Query(value = "{room_id: ?0, type: ?1, ntype: ?2, created_time: {$gt: ?3},  $maxDistance :?4}")
	@Query(value = "{room_id: ?0, type: ?1, ntype: ?2, created_time: {$gt: ?3}}")
	List<ChatMessageDao> findNearestMessage(String roomId, ChatMessageEnum type, ChatNotificationEnum nType, long createdTime, Pageable page);

	/*
	{
	    created_at: {
	        $gte: ISODate("2010-04-29T00:00:00.000Z"),
	        $lt: ISODate("2010-05-01T00:00:00.000Z")
	    }
	}
	*/
	
	@Query(value = "{room_id: ?0, created_time: {$gte: ?1, $lte: ?2}}")
	List<ChatMessageDao> findScopedMessageByRoomId(String roomId, long startTime, long endTime);

	
	//@Query(value = "{'room_id' : ?0, 'user_id' : ?1}")
	Long deleteByRoomId(String roomId);
	
	//@Query(value = "{'user_id' : ?1}")
	Long deleteByUserId(String userId);
}
