package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.seerstech.chat.server.constant.ErrorCodeEnum;

public class SuccessResponse {
	
	@JsonProperty("code")
	private ErrorCodeEnum code;
	
	@JsonProperty("message")
	private String message;
	
	public SuccessResponse() {
		this.code = ErrorCodeEnum.CODE_SUCCESS;
		this.message = ErrorCodeEnum.CODE_SUCCESS.toString();
	}
	
	public SuccessResponse(String message) {
		this.code = ErrorCodeEnum.CODE_SUCCESS;
		this.message = message;
	}
}
