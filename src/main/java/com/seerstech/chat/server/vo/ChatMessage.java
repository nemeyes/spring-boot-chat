package com.seerstech.chat.server.vo;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.seerstech.chat.server.constant.ChatMessageEnum;
import com.seerstech.chat.server.constant.ChatNotificationEnum;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {

	@JsonProperty("message_id")
	private String messageId;
	
	@JsonProperty("type")
	private ChatMessageEnum type; // 메시지 타입
	
	@Field("ntype")
	private ChatNotificationEnum nType;
	
	@JsonProperty("room_id")
	private String roomId;
	
	@JsonProperty("user_id")
	private String userId;

	@JsonProperty("message")
	private String message;

	@JsonProperty("parent_message_id")
	private String parentMessageId;
	
	@JsonProperty("parent_message")
	private ChatMessage parentMessage;
	
	@JsonProperty("file_mime_type")
	private String mimeType;
	
	@JsonProperty("file_download_path")
	private String downloadPath;
		
	@JsonProperty("created_time")
	private long createdTime;
	
	@JsonProperty("user_info")
	private ChatRoomUser userInfo;
	
	@JsonProperty("unread_user_id_list")
	private List<String> unreadUserIdList;

    @Builder
    public ChatMessage(String messageId, ChatMessageEnum messageType, String roomId, String userId, String message, String mimeType, String downloadPath, long createdTime) {
    	this.messageId = messageId;
        this.type = messageType;
        this.roomId = roomId;
        this.userId = userId;
        this.message = message;
        this.mimeType = mimeType;
        this.downloadPath = downloadPath;
        this.createdTime = createdTime;
    }
}