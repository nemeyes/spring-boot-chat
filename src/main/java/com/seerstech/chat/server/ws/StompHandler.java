package com.seerstech.chat.server.ws;

import com.seerstech.chat.server.jwt.JWTTokenParser;
import com.seerstech.chat.server.jwt.JWTTokenProvider;
import com.seerstech.chat.server.model.ChatUserDao;
import com.seerstech.chat.server.service.ChatUserDetailsService;
import com.seerstech.chat.server.service.ChatRoomService;
import com.seerstech.chat.server.service.ChatUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

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
    private final ChatRoomService mChatRoomService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT == accessor.getCommand()) {
        	
            String jwtToken = accessor.getFirstNativeHeader("Authorization");
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
        	
        	
        	UsernamePasswordAuthenticationToken userAuth = (UsernamePasswordAuthenticationToken)message.getHeaders().get("simpUser");
        	String userId = (String)userAuth.getPrincipal();
        	String destination = (String) message.getHeaders().get("simpDestination");
        	if(userId!=null && userId.length()>0 && destination!=null && destination.length()>0) {
        		String roomId = mChatRoomService.getRoomId(destination);
        		mChatRoomService.joinActiveRoom(userId, roomId);
        	}
            
        } else if (StompCommand.UNSUBSCRIBE == accessor.getCommand()) { // 채팅룸 구독요청
        	
        	UsernamePasswordAuthenticationToken userAuth = (UsernamePasswordAuthenticationToken)message.getHeaders().get("simpUser");
        	String userId = (String)userAuth.getPrincipal();
        	String destination = (String) message.getHeaders().get("simpDestination");
        	if(userId!=null && userId.length()>0 && destination!=null && destination.length()>0) {
        		String roomId = mChatRoomService.getRoomId(destination);
        		mChatRoomService.leaveActiveRoom(userId, roomId);
        	}
        	
        } else if (StompCommand.DISCONNECT == accessor.getCommand()) {
        	
        	UsernamePasswordAuthenticationToken userAuth = (UsernamePasswordAuthenticationToken)message.getHeaders().get("simpUser");
        	String userId = (String)userAuth.getPrincipal();
        	if(userId!=null && userId.length()>0) {
        		mChatRoomService.leaveActiveRoom(userId);
        	}
        }
        return message;
    }
}