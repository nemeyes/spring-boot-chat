package com.seerstech.chat.server.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRoomUserListResponse extends SuccessResponse {
	@JsonProperty("room_id")
	private String roomId;
	
	@JsonProperty("room_users")
	private List<ChatRoomUser> roomUsers;
}
