package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveUserFromRoomRequest {
	@JsonProperty("room_id")
	private String roomId;

	//@JsonProperty("user_id")
	//private String userId;
}
