package com.seerstech.chat.server.redis;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.seerstech.chat.server.model.ChatMessageEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedisChatMessage implements Serializable  {
	
	private static final long serialVersionUID = 8424960036408448526L;
	
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
	
	@JsonProperty("created_time")
	private long createdTime;
}
