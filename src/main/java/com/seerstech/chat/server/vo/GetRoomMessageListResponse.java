package com.seerstech.chat.server.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRoomMessageListResponse extends SuccessResponse {
	
	@JsonProperty("room_id")
	private String roomId;

	@JsonProperty("message_list")
	private List<ChatMessage> messageList;
	
	@JsonProperty("current_page")
	private int currentMessagePage;

	@JsonProperty("total_page")
	private int totalMessagePages;
	
	@JsonProperty("total_count")
	private Long totalMessageCount;
}
