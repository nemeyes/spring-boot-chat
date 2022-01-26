package com.seerstech.chat.server.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seerstech.chat.server.vo.ChatMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber {

    private final ObjectMapper mObjectMapper;
    private final SimpMessageSendingOperations mMessagingTemplate;

    public void sendMessage(String publishMessage) {
        try {
            ChatMessage message = mObjectMapper.readValue(publishMessage, ChatMessage.class);
            mMessagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
        } catch (Exception e) {
            log.error("Exception {}", e);
        }
    }
}