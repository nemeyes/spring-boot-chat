package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

public class ChatRoomUser {

	@JsonProperty("room_id")
	private String roomId;
	
	@JsonProperty("user_id")
	private String userId;

	@JsonProperty("user_nickname")
	private String userNickname;
	
	@JsonProperty("user_joined_room")
	private boolean userJoinedRoom;
	
    @Builder
    public ChatRoomUser(String roomId, String userId, String userNickname, boolean userJoinedRoom) {
    	this.roomId = roomId;
    	this.userId = userId;
    	this.userNickname = userNickname;
    	this.userJoinedRoom = userJoinedRoom;
    }
}
