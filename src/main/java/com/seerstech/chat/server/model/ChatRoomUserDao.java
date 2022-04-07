package com.seerstech.chat.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "ChatRoomUser")
public class ChatRoomUserDao extends BaseDao {

	@Id
	private String id;
	
	@Field("room_id")
    private String roomId;
	
	@Field("user_id")
    private String userId;
	
	@Field("user_joined_room")
	private Boolean userJoinedRoom;

	/*
	@Field("created_time")
	private long createdTime;
	
	@Field("deleted_time")
	private long deletedTime;
	*/
}