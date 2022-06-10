package com.seerstech.chat.server.controller;

import com.seerstech.chat.server.constant.ErrorCodeEnum;
import com.seerstech.chat.server.jwt.JWTTokenParser;
import com.seerstech.chat.server.jwt.JWTTokenProvider;
import com.seerstech.chat.server.service.ChatMessageService;
import com.seerstech.chat.server.utils.TimeUtil;
import com.seerstech.chat.server.vo.ChatMessage;
import com.seerstech.chat.server.vo.ErrorResponse;
import com.seerstech.chat.server.vo.GetRoomMessageListRequest;
import com.seerstech.chat.server.vo.GetRoomMessageListResponse;
import com.seerstech.chat.server.vo.MessageMarkAsDeleteRequest;
import com.seerstech.chat.server.vo.MessageMarkAsMultiReadRequest;
import com.seerstech.chat.server.vo.MessageMarkAsMultiReadResponse;
import com.seerstech.chat.server.vo.MessageMarkAsReadRequest;
import com.seerstech.chat.server.vo.MessageMarkAsReadResponse;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    public ResponseEntity<?> messages(@RequestBody GetRoomMessageListRequest req) {
    	if(req.getPageNumber()<1) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_PAGE_NUMBER_MUST_BE_GREATER_THAN_ZERO, ErrorCodeEnum.CODE_PAGE_NUMBER_MUST_BE_GREATER_THAN_ZERO.toString()));
    	}
    	
    	if(req.getPageSize()<1) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_PAGE_SIZE_MUST_BE_GREATER_THAN_ZERO, ErrorCodeEnum.CODE_PAGE_SIZE_MUST_BE_GREATER_THAN_ZERO.toString()));
    	}
    	
    	GetRoomMessageListResponse response = mChatService.getMessageListByRoomId(req.getRoomId(), req.getPageNumber(), req.getPageSize());
    	if(response!=null) {
    		return ResponseEntity.ok(response);   		
		} else {
			return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_NO_MESSAGE_IN_ROOM, ErrorCodeEnum.CODE_NO_MESSAGE_IN_ROOM.toString()));
		}
    }
    
    @CrossOrigin(origins = "*")
    @PostMapping("/chat/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("room_id") String roomId, @RequestParam("user_id") String userId, HttpServletRequest request) {
        return mChatService.upload(file, roomId, userId, request);
    }
    
    @CrossOrigin(origins = "*")
    @PostMapping("/chat/upload/comment")
    public ResponseEntity<?> uploadCommentFile(@RequestParam("file") MultipartFile file, @RequestParam("room_id") String roomId, @RequestParam("user_id") String userId, @RequestParam("parent_message_id") String parentMessageId, HttpServletRequest request) {
        return mChatService.uploadComment(file, roomId, userId, parentMessageId, request);
    }

    /*
    @PostMapping("/chat/uploads")
    public ResponseEntity<?> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files, @RequestParam("room_id") String roomId, @RequestParam("user_id") String userId, HttpServletRequest request) {
        return (ResponseEntity<?>) Arrays.asList(files).stream().map(file -> uploadFile(file, roomId, userId, request)).collect(Collectors.toList());
    }
    */
    
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
        mChatService.send(message);
    }
    
    @PostMapping("/chat/message/read")
    public ResponseEntity<?> read(@RequestHeader (name="Authorization") String token, @RequestBody MessageMarkAsReadRequest req) {
    	String accessToken = JWTTokenParser.parse(token);
        String userId = mJWTTokenProvider.getUserNameFromJwt(accessToken);
        
    	ErrorCodeEnum code = mChatService.markAsRead(req.getRoomId(), userId, req.getMessageId());
    	if(code==ErrorCodeEnum.CODE_SUCCESS) {
    		return ResponseEntity.ok(new MessageMarkAsReadResponse());
    	} else {
    		return ResponseEntity.ok(new ErrorResponse(code, code.toString()));
    	}
    }
    
    @PostMapping("/chat/message/mread")
    public ResponseEntity<?> mread(@RequestHeader (name="Authorization") String token, @RequestBody MessageMarkAsMultiReadRequest req) {
    	String accessToken = JWTTokenParser.parse(token);
        String userId = mJWTTokenProvider.getUserNameFromJwt(accessToken);
        
    	ErrorCodeEnum code = mChatService.markAsMultiRead(req.getRoomId(), userId, req.getMessageIds());
    	if(code==ErrorCodeEnum.CODE_SUCCESS) {
    		return ResponseEntity.ok(new MessageMarkAsMultiReadResponse());
    	} else {
    		return ResponseEntity.ok(new ErrorResponse(code, code.toString()));
    	}
    }
    
    @PostMapping("/chat/message/delete")
    public ResponseEntity<?> delete(@RequestHeader (name="Authorization") String token, @RequestBody MessageMarkAsDeleteRequest req) {
    	String accessToken = JWTTokenParser.parse(token);
        String userId = mJWTTokenProvider.getUserNameFromJwt(accessToken);
        
    	ErrorCodeEnum code = mChatService.markAsDelete(req.getMessageId(), userId);
    	if(code==ErrorCodeEnum.CODE_SUCCESS) {
    		return ResponseEntity.ok(new MessageMarkAsReadResponse());
    	} else {
    		return ResponseEntity.ok(new ErrorResponse(code, code.toString()));
    	}
    }
}