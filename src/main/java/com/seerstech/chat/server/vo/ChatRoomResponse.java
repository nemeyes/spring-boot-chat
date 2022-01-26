package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomResponse extends SuccessResponse {
	
	@JsonProperty("id")
	private String roomId;
	
	@JsonProperty("name")
	private String roomName;
	
	@JsonProperty("description")
	private String roomDescription;	
	
	public ChatRoomResponse(String id, String name, String description) {
		this.roomId = id;
		this.roomName = name;
		this.roomDescription = description;
	}

}
