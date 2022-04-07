package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatUser {
	@JsonProperty("user_id")
	private String userId;

	@JsonProperty("user_nickname")
	private String userNickname;
	
    @Builder
    public ChatUser(String userId, String userNickname, boolean isJoined) {
    	this.userId = userId;
    	this.userNickname = userNickname;
    }
}
