package com.seerstech.chat.server.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.seerstech.chat.server.model.ChatMessageEnum;

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
		
	@JsonProperty("created_time")
	private long createdTime;
	
	@JsonProperty("participants")
	private List<ChatUser> participants;

    @Builder
    public ChatMessage(String messageId, ChatMessageEnum messageType, String roomId, String userId, String message, long createdTime) {
    	this.messageId = messageId;//UUID.randomUUID().toString();
        this.type = messageType;
        this.roomId = roomId;
        this.userId = userId;
        this.message = message;
        this.createdTime = createdTime;//TimeUtil.unixTime();
    }
}