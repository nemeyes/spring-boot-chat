package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoom {
	
	@JsonProperty("room_id")
	private String roomId;
	
	@JsonProperty("room_name")
	private String roomName;
	
	@JsonProperty("room_description")
	private String roomDescription;	
	
	@JsonProperty("room_unread_count")
	private int messageCountUnread;
	
	@JsonProperty("room_last_message")
	private ChatMessage roomLastMessage;
	
	public ChatRoom(String id, String name, String description, int messageCountUnread) {
		this.roomId = id;
		this.roomName = name;
		this.roomDescription = description;
		this.messageCountUnread = messageCountUnread;
	}

}
