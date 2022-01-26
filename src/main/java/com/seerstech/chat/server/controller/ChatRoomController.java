package com.seerstech.chat.server.controller;

import com.seerstech.chat.server.jwt.JWTTokenParser;
import com.seerstech.chat.server.jwt.JWTTokenProvider;
import com.seerstech.chat.server.model.ChatRoleDao;
import com.seerstech.chat.server.model.ChatRoleEnum;
import com.seerstech.chat.server.model.ChatRoomDao;
import com.seerstech.chat.server.model.ChatUserDao;
import com.seerstech.chat.server.service.ChatRoomService;
import com.seerstech.chat.server.service.ChatUserDetailsService;
import com.seerstech.chat.server.vo.CreateRoomRequest;
import com.seerstech.chat.server.vo.ErrorCodeEnum;
import com.seerstech.chat.server.vo.ErrorResponse;
import com.seerstech.chat.server.vo.GetRoomUsersRequest;
import com.seerstech.chat.server.vo.GetRoomUsersResponse;
import com.seerstech.chat.server.vo.InviteRoomRequest;
import com.seerstech.chat.server.vo.JoinRoomRequest;
import com.seerstech.chat.server.vo.LeaveRoomRequest;
import com.seerstech.chat.server.vo.NotifyRoomMessageRequest;
import com.seerstech.chat.server.vo.SuccessResponse;
import com.seerstech.chat.server.vo.ChatRoomResponse;
import com.seerstech.chat.server.vo.ChatUser;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/chat")
public class ChatRoomController {
	
	@Autowired
    private JWTTokenProvider mJWTTokenProvider;

    private final ChatRoomService mChatRoomService;
    private final ChatUserDetailsService mChatUserService;

    @GetMapping("/room")
    @ResponseBody
    public List<ChatRoomResponse> list(@RequestHeader (name="Authorization") String token) {
    	String jwtToken = JWTTokenParser.parse(token);
    	String userId = mJWTTokenProvider.getUserNameFromJwt(jwtToken);
    	
    	ChatUserDao user = mChatUserService.findUserByUserId(userId);
    	boolean bAdmin = false;
    	Iterator<ChatRoleDao> iter = user.getUserRoles().iterator();
    	while(iter.hasNext()) {
    		ChatRoleDao role = iter.next();
    		if(role.getRole()==ChatRoleEnum.ROLE_ADMIN) {
    			bAdmin = true;
    		}
    	}
    	
    	List<ChatRoomResponse> roomsResponse = new ArrayList<ChatRoomResponse>();
    	if(bAdmin) {
	        List<ChatRoomDao> rooms = mChatRoomService.findRoom();
	        rooms.stream().forEach(room -> {
	        	ChatRoomResponse roomResponse = new ChatRoomResponse(room.getRoomId(), room.getRoomName(), room.getRoomDescription());
	        	roomsResponse.add(roomResponse);
	        });
    	} else {
	        List<ChatRoomDao> rooms = mChatRoomService.findJoinedRoom(userId);
	        rooms.stream().forEach(room -> {
	        	ChatRoomResponse roomResponse = new ChatRoomResponse(room.getRoomId(), room.getRoomName(), room.getRoomDescription());
	        	roomsResponse.add(roomResponse);
	        });
    	}
        return roomsResponse;
    }

    @PostMapping("/room/create")
    @ResponseBody
    public ResponseEntity<?> create(@RequestHeader (name="Authorization") String token, @RequestBody CreateRoomRequest req) {
    	String jwtToken = JWTTokenParser.parse(token);
    	String userId = mJWTTokenProvider.getUserNameFromJwt(jwtToken);
    	ChatRoomDao room = mChatRoomService.createRoom(req.getRoomName(), req.getRoomDescription(), userId, req.getParticipants());
    	return ResponseEntity.ok(new ChatRoomResponse(room.getRoomId(), room.getRoomName(), room.getRoomDescription()));
    }
    
    @PostMapping("/room/join")
    @ResponseBody
    public ResponseEntity<?> join(@RequestHeader (name="Authorization") String token, @RequestBody JoinRoomRequest req) {
    	String jwtToken = JWTTokenParser.parse(token);
    	String userId = mJWTTokenProvider.getUserNameFromJwt(jwtToken);
    	mChatRoomService.joinRoom(userId, req.getRoomId());
    	return ResponseEntity.ok(new SuccessResponse("join success"));
    }
    
    @PostMapping("/room/invite")
    @ResponseBody
    public ResponseEntity<?> invite(@RequestBody InviteRoomRequest req) {
    	boolean existUser = mChatUserService.existsByUserId(req.getUserId());
    	boolean existRoom = mChatRoomService.existRoomByRoomId(req.getRoomId());
    	if(existUser) {
        	boolean exist = mChatRoomService.existUserInRoom(req.getUserId(), req.getRoomId());
        	if(!exist) {
    	    	mChatRoomService.joinRoom(req.getUserId(), req.getRoomId());
    	    	return ResponseEntity.ok(new SuccessResponse("invite success"));
        	} else {
        		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_USER_ALREADY_EXIST_IN_ROOM, ErrorCodeEnum.CODE_USER_ALREADY_EXIST_IN_ROOM.toString()));
        	}   
    	} else if(!existUser) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_USER_NOT_FOUND, ErrorCodeEnum.CODE_USER_NOT_FOUND.toString()));
    	} else if(!existRoom) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_ROOM_NOT_EXIST, ErrorCodeEnum.CODE_ROOM_NOT_EXIST.toString()));
    	} else {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_ERROR_INVITE_USER_TO_ROOM, ErrorCodeEnum.CODE_ERROR_INVITE_USER_TO_ROOM.toString()));
    	}
    }
    
    @PostMapping("/room/leave")
    @ResponseBody
    public ResponseEntity<?> leave(@RequestHeader (name="Authorization") String token, @RequestBody LeaveRoomRequest req) {
    	String jwtToken = JWTTokenParser.parse(token);
    	String userId = mJWTTokenProvider.getUserNameFromJwt(jwtToken);
    	mChatRoomService.leaveRoom(userId, req.getRoomId());
    	return ResponseEntity.ok(new SuccessResponse("leave success"));
    }
    
    @PostMapping("/room/notify")
    @ResponseBody
    public ResponseEntity<?> notify(@RequestHeader (name="Authorization") String token, @RequestBody NotifyRoomMessageRequest req) {
    	String jwtToken = JWTTokenParser.parse(token);
    	String userId = mJWTTokenProvider.getUserNameFromJwt(jwtToken);
    	ChatUserDao user = mChatUserService.findUserByUserId(userId);
    	
    	boolean allowed = false;
    	Iterator<ChatRoleDao> iter = user.getUserRoles().iterator();
    	while(iter.hasNext()) {
    		ChatRoleDao role = iter.next();
    		if(role.getRole()==ChatRoleEnum.ROLE_ADMIN) {
    			allowed = true;
    		}
    	}
    	if(allowed) {
    		List<String> roomIds = req.getRoomIds();
    		roomIds.forEach(roomId->{
    			mChatRoomService.notifyRoomMessage(roomId, req.getRoomNotification());
    		});
    		return ResponseEntity.ok(new SuccessResponse("notification is sended"));
    	} else {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_ERROR_NOTIFY_TO_ROOM, ErrorCodeEnum.CODE_ERROR_NOTIFY_TO_ROOM.toString()));
    	}
    }
    
    
    @PostMapping("/room/users")
    @ResponseBody
    public ResponseEntity<?> users(@RequestBody GetRoomUsersRequest req) {
    	List<ChatUserDao> users = mChatRoomService.getRoomUsers(req.getRoomId());
    	if(users!=null) {
        	GetRoomUsersResponse response = new GetRoomUsersResponse();
        	List<ChatUser> participants = new ArrayList<ChatUser>();
        	users.forEach(user->{
        		ChatUser participant = ChatUser.builder().userId(user.getUserId()).userNickname(user.getUserNickname()).build();
        		participants.add(participant);
        	});
        	response.setParticipants(participants);
        	response.setRoomId(req.getRoomId());    
        	return ResponseEntity.ok(response);
    	} else {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_NO_USER_EXIST_IN_ROOM, ErrorCodeEnum.CODE_NO_USER_EXIST_IN_ROOM.toString()));
    	}	
    }

    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ResponseEntity<?> info(@PathVariable String roomId) {
    	ChatRoomDao room = mChatRoomService.findRoomByRoomId(roomId);
    	if(room==null) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_ROOM_NOT_EXIST, ErrorCodeEnum.CODE_ROOM_NOT_EXIST.toString()));
    	} else {
    		return ResponseEntity.ok(new ChatRoomResponse(room.getRoomId(), room.getRoomName(), room.getRoomDescription()));
    	}
    }
}