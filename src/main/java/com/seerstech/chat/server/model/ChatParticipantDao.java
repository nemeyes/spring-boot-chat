package com.seerstech.chat.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "ChatParticipant")
public class ChatParticipantDao {

	@Id
	private String id;
	
	@Field("room_id")
    private String roomId;
	
	@Field("user_id")
    private String userId;

	@Field("created_time")
	private long createdTime;
	
}