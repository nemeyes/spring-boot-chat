package com.seerstech.chat.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "ChatMessageStatus")
public class ChatMessageStatusDao extends BaseDao {
	@Id
	private String id;
	
	@Field("user_id")
	private String userId;
	
	@Field("room_id")
	private String roomId;
	
	@Field("message_id")
	private String messageId;
}
