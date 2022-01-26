package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRoomUsersRequest {
	@JsonProperty("room_id")
	private String roomId;
}
