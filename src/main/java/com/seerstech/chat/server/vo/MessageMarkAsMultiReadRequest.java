package com.seerstech.chat.server.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageMarkAsMultiReadRequest {
	
	@JsonProperty("room_id")
	private String roomId;
	
	@JsonProperty("message_ids")
	private List<String> messageIds;
	
}
