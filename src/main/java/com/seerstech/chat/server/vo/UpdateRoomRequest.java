package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoomRequest {
	@JsonProperty("room_id")
	private String roomId;
	
	@JsonProperty("room_name")
	private String roomName;
	
	@JsonProperty("room_description")
	private String roomDescription;	

}
