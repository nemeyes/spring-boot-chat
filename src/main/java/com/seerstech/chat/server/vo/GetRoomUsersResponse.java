package com.seerstech.chat.server.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRoomUsersResponse extends SuccessResponse {
	@JsonProperty("room_id")
	private String roomId;
	
	@JsonProperty("participants")
	private List<ChatUser> participants;
}
