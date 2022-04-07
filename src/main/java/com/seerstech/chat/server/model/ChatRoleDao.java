package com.seerstech.chat.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.seerstech.chat.server.constant.ChatRoleEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "ChatRole")
public class ChatRoleDao extends BaseDao {
	
    @Id
    private String id;

    @Field("role")
    private ChatRoleEnum role;
}