package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoomResponse extends SuccessResponse {
	@JsonProperty("room_id")
	private String roomId;
	
	@JsonProperty("room_name")
	private String roomName;
	
	@JsonProperty("room_description")
	private String roomDescription;	
	
	public UpdateRoomResponse(String roomId, String roomName, String roomDescription) {
		this.roomId = roomId;
		this.roomName = roomName;
		this.roomDescription = roomDescription;
	}
	
}