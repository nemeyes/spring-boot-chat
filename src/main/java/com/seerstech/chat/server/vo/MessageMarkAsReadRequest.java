package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageMarkAsReadRequest {
	
	@JsonProperty("room_id")
	private String roomId;
	
	@JsonProperty("message_id")
	private String messageId;
	
}
