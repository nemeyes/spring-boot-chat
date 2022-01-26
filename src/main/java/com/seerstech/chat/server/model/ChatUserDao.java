package com.seerstech.chat.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Document(collection = "ChatUser")
public class ChatUserDao {
	
	@Id
	private String id;
	
	@Field("user_id")
	private String userId;
	
	@Field("user_password")
	private String userPassword;
	
	@Field("user_nickname")
	private String userNickname;
	
	@Field("user_enabled")
	private boolean enabled;
	
	@Field("user_roles")
	@DBRef
	private Set<ChatRoleDao> userRoles = new HashSet<>();
	
	@Field("created_time")
	private long createdTime;
}