package com.seerstech.chat.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "ChatRoom")
public class ChatRoomDao extends BaseDao {

	@Id
	private String id;
	
	@Field("room_id")
    private String roomId;
	
	@Field("room_name")
    private String roomName;
	
	@Field("room_description")
	private String roomDescription;
	
	/*
	@Field("created_time")
	private long createdTime;
	*/
}