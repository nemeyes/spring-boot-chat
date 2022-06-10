package com.seerstech.chat.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.seerstech.chat.server.constant.ChatMessageEnum;
import com.seerstech.chat.server.constant.ChatNotificationEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "ChatMessage")
public class ChatMessageDao extends BaseDao {
	
	@Id
	private String id;
	
	@Field("message_id")
	private String messageId;
	
	@Field("type")
	private ChatMessageEnum type;
	
	@Field("ntype")
	private ChatNotificationEnum nType;
	
	@Field("room_id")
	private String roomId;
	
	@Field("user_id")
	private String userId;

	@Field("message")
	private String message;

	@Field("parent_message_id")
	private String parentMessageId;
	
	@Field("file_mime_type")
	private String mimeType;
	
	@Field("file_download_path")
	private String downloadPath;
	
	@Field("file_original_name")
	private String originalFileName;
	
    public ChatMessageDao(String messageId, ChatMessageEnum type, 
    						String roomId, String userId, 
    						String message, String parentMessageId, 
    						long createdTime) {
    	this.messageId = messageId;
        this.type = type;
        this.roomId = roomId;
        this.userId = userId;
        this.message = message;
        this.parentMessageId = parentMessageId;
        this.createdTime = createdTime;
    }
}