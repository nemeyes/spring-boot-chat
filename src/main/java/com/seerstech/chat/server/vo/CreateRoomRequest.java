package com.seerstech.chat.server.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoomRequest {
	
	@JsonProperty("room_name")
	private String roomName;
	
	@JsonProperty("room_description")
	private String roomDescription;
	
	@JsonProperty("room_users")
	private List<String> roomUsers;
}
