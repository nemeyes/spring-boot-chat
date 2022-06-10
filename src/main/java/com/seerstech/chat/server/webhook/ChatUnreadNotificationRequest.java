package com.seerstech.chat.server.webhook;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.seerstech.chat.server.vo.ChatMessage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatUnreadNotificationRequest {

	@JsonProperty("message")
	private ChatMessage message;
	
	@JsonProperty("users")
	private List<String> users;
}
