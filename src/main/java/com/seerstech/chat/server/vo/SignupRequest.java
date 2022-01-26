package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class SignupRequest {
	
	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("user_password")
	private String userPassword;
	
	@JsonProperty("user_nickname")
	private String userNickname;
	
	@JsonProperty("user_roles")
	private Set<String> userRoles;
}
