package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReissueRequest {
		
	@JsonProperty("refresh_token")
	private String refreshToken;
	
	@JsonProperty("access_token")
	private String accessToken;

}
