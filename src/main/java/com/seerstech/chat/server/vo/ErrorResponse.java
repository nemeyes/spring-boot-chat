package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
	
	@JsonProperty("code")
	private ErrorCodeEnum code;

	@JsonProperty("message")
	private String message;
	
	public ErrorResponse(ErrorCodeEnum code, String message) {
		this.code = code;
		this.message = message;
	}
}
