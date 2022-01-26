package com.seerstech.chat.server.ws;

import com.seerstech.chat.server.jwt.JWTTokenParser;
import com.seerstech.chat.server.jwt.JWTTokenProvider;
import com.seerstech.chat.server.model.ChatUserDao;
import com.seerstech.chat.server.service.ChatUserDetailsService;
import com.seerstech.chat.server.service.ChatUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final JWTTokenProvider mJWTTokenProvider;
    private final ChatUserDetailsService mChatUserDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT == accessor.getCommand()) {
        	
            String jwtToken = accessor.getFirstNativeHeader("Authorization");
            log.info("CONNECT {}", jwtToken);
            String jwt = JWTTokenParser.parse(jwtToken);
            if(mJWTTokenProvider.validateToken(jwt)) {
            	//String sessionId = (String) message.getHeaders().get("simpSessionId");
            	String userId = mJWTTokenProvider.getUserNameFromJwt(jwt);
            	ChatUserDao user = mChatUserDetailsService.findUserByUserId(userId);
            	ChatUserDetails userDetails = (ChatUserDetails)mChatUserDetailsService.loadUserByUsername(userId);
            	
            	//ChatUserNicknameManager.getInstance().putUserNickname(sessionId, userDetails.getUsername(), userDetails.getUserNickname());
            	final UsernamePasswordAuthenticationToken userAuth = new UsernamePasswordAuthenticationToken(user.getUserId(), user.getUserPassword(), userDetails.getAuthorities());
            	accessor.setUser(userAuth);
            }
            
        } else if (StompCommand.SUBSCRIBE == accessor.getCommand()) { // 채팅룸 구독요청
        	
            //String roomId = mChatService.getRoomId(Optional.ofNullable((String) message.getHeaders().get("simpDestination")).orElse("InvalidRoomId"));
            //String sessionId = (String) message.getHeaders().get("simpSessionId");
            //mChatRoomService.joinActiveRoom(sessionId, roomId);
            //mChatRoomService.plusUserCount(roomId);
            //String name = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnknownUser");
            //mChatService.sendChatMessage(ChatMessage.builder().type(ChatMessageEnum.MSG_ENTER).roomId(roomId).userId(name).build());
            //log.info("SUBSCRIBED {}, {}", name, roomId);
            
        } else if (StompCommand.DISCONNECT == accessor.getCommand()) {
        	
            //String sessionId = (String) message.getHeaders().get("simpSessionId");
            //String roomId = mChatRoomService.getJoinedActiveRoom(sessionId);
            //mChatRoomService.minusUserCount(roomId);
            //String name = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnknownUser");
            //mChatService.sendChatMessage(ChatMessage.builder().type(ChatMessageEnum.MSG_QUIT).roomId(roomId).userId(name).build());
            //mChatRoomService.leaveActiveRoom(sessionId);
            //log.info("DISCONNECTED {}, {}", sessionId, roomId);
            
        }
        return message;
    }
}