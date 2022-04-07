package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRoomListRequest {
	@JsonProperty("page")
	private int pageNumber;
	
	@JsonProperty("page_size")
	private int pageSize;
}
