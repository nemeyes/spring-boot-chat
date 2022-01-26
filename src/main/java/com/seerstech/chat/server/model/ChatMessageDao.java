package com.seerstech.chat.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "ChatMessage")
public class ChatMessageDao {
	
	@Id
	private String id;
	
	@JsonProperty("message_id")
	private String messageId;
	
	@Field("type")
	private ChatMessageEnum type; // 메시지 타입
	
	@Field("room_id")
	private String roomId;
	
	@Field("user_id")
	private String userId;

	@Field("message")
	private String message;

	@Field("parent_message_id")
	private String parentMessageId;
	
	@Field("created_time")
	private long createdTime;
	
    public ChatMessageDao(String messageId, ChatMessageEnum type, 
    						String roomId, String userId, 
    						String message, String parentMessageId, 
    						long ct) {
    	this.messageId = messageId;
        this.type = type;
        this.roomId = roomId;
        this.userId = userId;
        this.message = message;
        this.parentMessageId = parentMessageId;
        this.createdTime = ct;
    }
}