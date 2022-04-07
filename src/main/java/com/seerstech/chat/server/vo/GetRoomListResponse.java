package com.seerstech.chat.server.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRoomListResponse extends SuccessResponse {
	@JsonProperty("room_list")
	private List<ChatRoom> roomList;
	
	@JsonProperty("current_page")
	private int currentPage;

	@JsonProperty("total_page")
	private int totalPages;
	
	@JsonProperty("total_count")
	private Long totalCount;
}
