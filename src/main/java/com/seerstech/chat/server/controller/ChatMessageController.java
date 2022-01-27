package com.seerstech.chat.server.controller;

import com.seerstech.chat.server.jwt.JWTTokenParser;
import com.seerstech.chat.server.jwt.JWTTokenProvider;
import com.seerstech.chat.server.service.ChatMessageService;
import com.seerstech.chat.server.utils.TimeUtil;
import com.seerstech.chat.server.vo.ChatMessage;
import com.seerstech.chat.server.vo.ErrorCodeEnum;
import com.seerstech.chat.server.vo.ErrorResponse;
import com.seerstech.chat.server.vo.GetRoomMessagesRequest;
import com.seerstech.chat.server.vo.GetRoomMessagesResponse;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Controller
public class ChatMessageController {

    private final JWTTokenProvider mJWTTokenProvider;
    private final ChatMessageService mChatService;
    
    @PostMapping("/chat/messages")
    @ResponseBody
    public ResponseEntity<?> messages(@RequestBody GetRoomMessagesRequest req) {
    	List<ChatMessage> messages = mChatService.getMessagesByRoomId(req.getRoomId());
    	if(messages!=null && messages.size()>0) {
    		GetRoomMessagesResponse response = new GetRoomMessagesResponse();
    		response.setRoomId(req.getRoomId());
    		response.setMessages(messages);
    		return ResponseEntity.ok(response);   		
		} else {
			return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_NO_MESSAGE_IN_ROOM, ErrorCodeEnum.CODE_NO_MESSAGE_IN_ROOM.toString()));
		}
    }
    
    @PostMapping("/chat/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("room_id") String roomId, @RequestParam("user_id") String userId, HttpServletRequest request) {
        return mChatService.upload(file, roomId, userId, request);
    }

    @PostMapping("/chat/uploads")
    public ResponseEntity<?> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files, @RequestParam("room_id") String roomId, @RequestParam("user_id") String userId, HttpServletRequest request) {
        return (ResponseEntity<?>) Arrays.asList(files).stream().map(file -> uploadFile(file, roomId, userId, request)).collect(Collectors.toList());
    }
    
    @GetMapping("/chat/download/{fileName:.+}")
    public ResponseEntity<?> download(@PathVariable String fileName, HttpServletRequest request) {
    	return mChatService.download(fileName, request);
    }

    @MessageMapping("/chat/message")
    public void message(ChatMessage message, @Header("Authorization") String token) {
    	String accessToken = JWTTokenParser.parse(token);
        String userId = mJWTTokenProvider.getUserNameFromJwt(accessToken);
        message.setMessageId(UUID.randomUUID().toString());
        message.setUserId(userId);
        message.setCreatedTime(TimeUtil.unixTime());
        mChatService.sendChatMessage(message);
    }
}