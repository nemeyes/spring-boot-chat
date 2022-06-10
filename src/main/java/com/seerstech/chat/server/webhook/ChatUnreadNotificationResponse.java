package com.seerstech.chat.server.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.seerstech.chat.server.constant.ErrorCodeEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatUnreadNotificationResponse {

	@JsonProperty("code")
	private ErrorCodeEnum code;
	
	@JsonProperty("message")
	private String message;
}
