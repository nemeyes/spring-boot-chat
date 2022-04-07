package com.seerstech.chat.server.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.seerstech.chat.server.constant.ChatNotificationEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotifyRoomMessageRequest {
	@JsonProperty("room_ids")
	private List<String> roomIds;
	
	@JsonProperty("notification_type")
	private ChatNotificationEnum nType;
	
	@JsonProperty("room_notification")
	private String roomNotification;
}
