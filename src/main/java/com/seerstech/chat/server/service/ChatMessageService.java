package com.seerstech.chat.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seerstech.chat.server.redis.RedisChatMessage;
import com.seerstech.chat.server.vo.ChatMessage;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ChatMessageService {
	
    private static final String CHAT_MESSAGE_BY_TIME = "CMBT_"; // 시간순서별 메시지 
    private static final String CHAT_MESSAGE_BY_RELATIONSHIP = "CMBR_"; // 연관관계
	
    @Value("${spring.message.count}")
    private int mMessageCount;
    
    private final ChannelTopic mChannelTopic;
    private final RedisTemplate<?, ?> redisTemplate;
    
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> mRoomMessage;
    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> mRoomMessageSeq;
    
    public String getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf('/');
        if (lastIndex != -1)
            return destination.substring(lastIndex + 1);
        else
            return "";
    }
    
    public List<ChatMessage> getMessagesByRoomId(String roomId) {
        Long size = mRoomMessageSeq.size(roomId);
        Long end = size;
        Long begin = 0L;
        if((size - mMessageCount)>0) {
        	begin = size - mMessageCount;
        }
        
        ObjectMapper objMapper = new ObjectMapper();
        ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
        
        Set<String> messageIds = mRoomMessageSeq.rangeByScore(roomId, begin, end);
        Iterator<String> iter = messageIds.iterator();
        while(iter.hasNext()) {
        	try {
        		String messageId = iter.next();
        		String jsonMessage = mRoomMessage.get(CHAT_MESSAGE_BY_TIME + roomId, messageId);
        		if(jsonMessage!=null && jsonMessage.length()>0) {
					RedisChatMessage converted = objMapper.readValue(jsonMessage, RedisChatMessage.class);
					
					RedisChatMessage convertedParent = null;
					String parentMessageId = mRoomMessage.get(CHAT_MESSAGE_BY_RELATIONSHIP + roomId, messageId);
					if(parentMessageId!=null && parentMessageId.length()>0) {
						String parentJsonMessage = mRoomMessage.get(CHAT_MESSAGE_BY_TIME + roomId, parentMessageId);
						if(parentJsonMessage!=null && parentJsonMessage.length()>0) {
							convertedParent = objMapper.readValue(parentJsonMessage, RedisChatMessage.class);
						}
					}
					
					ChatMessage message = ChatMessage.builder()
							.messageId(converted.getMessageId())
							.messageType(converted.getType())
							.roomId(converted.getRoomId())
							.userId(converted.getUserId())
							.message(converted.getMessage())
							.createdTime(converted.getCreatedTime())
							.build();
					
					if(convertedParent!=null) {
						ChatMessage parentMessage = ChatMessage.builder()
														.messageId(convertedParent.getMessageId())
														.messageType(convertedParent.getType())
														.roomId(convertedParent.getRoomId())
														.userId(convertedParent.getUserId())
														.message(convertedParent.getMessage())
														.createdTime(convertedParent.getCreatedTime())
														.build();
						message.setParentMessage(parentMessage);
					}
					messages.add(message);
        		}
        	} catch (JsonProcessingException e) {}
        }
        return messages;
    }

    public void sendChatMessage(ChatMessage message) {
    	RedisChatMessage redisMessage = new RedisChatMessage();
    	redisMessage.setMessageId(message.getMessageId());
    	redisMessage.setType(message.getType());
    	redisMessage.setRoomId(message.getRoomId());
    	redisMessage.setUserId(message.getUserId());
    	redisMessage.setMessage(message.getMessage());
    	redisMessage.setCreatedTime(message.getCreatedTime());
    	
    	ObjectMapper objMapper = new ObjectMapper();
    	try {
    		String jsonRedisMessage  = objMapper.writeValueAsString(redisMessage);
    		mRoomMessage.put(CHAT_MESSAGE_BY_TIME + message.getRoomId(), redisMessage.getMessageId(), jsonRedisMessage);
    		
        	if(message.getParentMessageId()!=null && message.getParentMessageId().length()>0) {
        		mRoomMessage.put(CHAT_MESSAGE_BY_RELATIONSHIP + message.getRoomId(), redisMessage.getMessageId(), message.getParentMessageId());
        		String parentMessageId = mRoomMessage.get(CHAT_MESSAGE_BY_RELATIONSHIP + message.getRoomId(), redisMessage.getMessageId());

				RedisChatMessage convertedParent = null;
				if(parentMessageId!=null && parentMessageId.length()>0) {
					String parentJsonMessage = mRoomMessage.get(CHAT_MESSAGE_BY_TIME + message.getRoomId(), parentMessageId);
					if(parentJsonMessage!=null && parentJsonMessage.length()>0) {
						convertedParent = objMapper.readValue(parentJsonMessage, RedisChatMessage.class);
						if(convertedParent!=null) {
							ChatMessage parentMessage = ChatMessage.builder()
															.messageId(convertedParent.getMessageId())
															.messageType(convertedParent.getType())
															.roomId(convertedParent.getRoomId())
															.userId(convertedParent.getUserId())
															.message(convertedParent.getMessage())
															.createdTime(convertedParent.getCreatedTime())
															.build();
							message.setParentMessage(parentMessage);
						}
					}
				}
        	}
    		
    		Long size = mRoomMessageSeq.size(message.getRoomId());
    		mRoomMessageSeq.add(message.getRoomId(), redisMessage.getMessageId(), size + 1);

            redisTemplate.convertAndSend(mChannelTopic.getTopic(), message);
		} catch (JsonProcessingException e) {}
    }

}