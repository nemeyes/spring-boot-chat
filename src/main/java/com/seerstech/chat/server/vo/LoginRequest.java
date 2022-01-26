package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
	
	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("user_password")
	private String userPassword;
	
}
