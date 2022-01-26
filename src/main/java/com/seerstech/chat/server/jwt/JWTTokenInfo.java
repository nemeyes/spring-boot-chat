package com.seerstech.chat.server.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class JWTTokenInfo {
	private String grantType;
	private String accessToken;
	private String refreshToken;
	private Long refreshTokenExpirationTime;
}
