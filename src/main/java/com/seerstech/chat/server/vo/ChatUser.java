package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatUser {
	@JsonProperty("id")
	private String id;

	@JsonProperty("name")
	private String name;
	
    @Builder
    public ChatUser(String userId, String userNickname) {
    	this.id = userId;
    	this.name = userNickname;
    }
	
}
