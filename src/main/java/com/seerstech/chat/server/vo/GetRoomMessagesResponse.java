package com.seerstech.chat.server.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRoomMessagesResponse extends SuccessResponse {
	
	@JsonProperty("room_id")
	private String roomId;

	@JsonProperty("messages")
	List<ChatMessage> messages;
	
	/*
	@JsonProperty("name")
	private String roomName;
	
	@JsonProperty("description")
	private String roomDescription;	
	

	public GetRoomMessagesResponse(String id, String name, String description) {
		this.roomId = id;
		this.roomName = name;
		this.roomDescription = description;
	}
	*/

}
