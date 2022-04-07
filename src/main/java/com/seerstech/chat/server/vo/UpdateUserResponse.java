package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateUserResponse extends SuccessResponse {
	
	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("user_nickname")
	private String userNickname;
	
	public UpdateUserResponse(String id, String nickname) {
		this.userId = id;
		this.userNickname = nickname;
	}
}
