package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistWebhookResponse extends SuccessResponse {
	@JsonProperty("base_url")
	private String baseUrl;
	
	@JsonProperty("path")
	private String path;
}
