package com.seerstech.chat.server.model;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseDao {
	@Field("created_time")
	protected long createdTime;
	
	@Field("deleted_time")
	protected long deletedTime;
}
