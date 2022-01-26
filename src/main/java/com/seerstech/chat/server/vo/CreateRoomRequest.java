package com.seerstech.chat.server.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoomRequest {
	
	@JsonProperty("name")
	private String roomName;
	
	@JsonProperty("description")
	private String roomDescription;
	
	@JsonProperty("participants")
	private List<String> participants;
}
