package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ReissueResponse extends SuccessResponse {
	
	@JsonProperty("grant_type")
	private String grantType;	
	
	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("refresh_token")
	private String refreshToken;
	
	@JsonProperty("refresh_token_expiration_time")
	private long refreshTokenExpirationTime;
}
