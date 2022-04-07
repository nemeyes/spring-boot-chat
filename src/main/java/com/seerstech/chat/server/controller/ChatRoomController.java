package com.seerstech.chat.server.controller;

import com.seerstech.chat.server.constant.ChatRoleEnum;
import com.seerstech.chat.server.constant.ErrorCodeEnum;
import com.seerstech.chat.server.jwt.JWTTokenParser;
import com.seerstech.chat.server.jwt.JWTTokenProvider;
import com.seerstech.chat.server.model.ChatRoleDao;
import com.seerstech.chat.server.model.ChatRoomDao;
import com.seerstech.chat.server.model.ChatUserDao;
import com.seerstech.chat.server.service.ChatMessageService;
import com.seerstech.chat.server.service.ChatRoomService;
import com.seerstech.chat.server.service.ChatUserDetailsService;
import com.seerstech.chat.server.vo.CreateRoomRequest;
import com.seerstech.chat.server.vo.CreateRoomResponse;
import com.seerstech.chat.server.vo.DeleteRoomRequest;
import com.seerstech.chat.server.vo.DeleteRoomResponse;
import com.seerstech.chat.server.vo.DeleteSectionRequest;
import com.seerstech.chat.server.vo.DeleteSectionResponse;
import com.seerstech.chat.server.vo.ErrorResponse;
import com.seerstech.chat.server.vo.GetRoomListRequest;
import com.seerstech.chat.server.vo.GetRoomListResponse;
import com.seerstech.chat.server.vo.GetRoomNotifyListRequest;
import com.seerstech.chat.server.vo.GetRoomNotifyListResponse;
import com.seerstech.chat.server.vo.GetRoomUserListRequest;
import com.seerstech.chat.server.vo.GetRoomUserListResponse;
import com.seerstech.chat.server.vo.InviteUserToRoomRequest;
import com.seerstech.chat.server.vo.InviteUserToRoomResponse;
import com.seerstech.chat.server.vo.JoinRoomRequest;
import com.seerstech.chat.server.vo.LeaveUserFromRoomRequest;
import com.seerstech.chat.server.vo.LeaveUserFromRoomResponse;
import com.seerstech.chat.server.vo.NotifyRoomMessageRequest;
import com.seerstech.chat.server.vo.NotifyRoomMessageResponse;
import com.seerstech.chat.server.vo.SuccessResponse;
import com.seerstech.chat.server.vo.UpdateRoomRequest;
import com.seerstech.chat.server.vo.UpdateRoomResponse;
import com.seerstech.chat.server.vo.ChatMessage;
import com.seerstech.chat.server.vo.ChatRoom;
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
    private final ChatMessageService mChatMessageService;

    @PostMapping("/rooms")
    @ResponseBody
    public ResponseEntity<?> list(@RequestHeader (name="Authorization") String token, @RequestBody GetRoomListRequest req) {
    	if(req.getPageNumber()<1) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_PAGE_NUMBER_MUST_BE_GREATER_THAN_ZERO, ErrorCodeEnum.CODE_PAGE_NUMBER_MUST_BE_GREATER_THAN_ZERO.toString()));
    	}
    	
    	if(req.getPageSize()<1) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_PAGE_SIZE_MUST_BE_GREATER_THAN_ZERO, ErrorCodeEnum.CODE_PAGE_SIZE_MUST_BE_GREATER_THAN_ZERO.toString()));
    	}
    	
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
    	
    	GetRoomListResponse response = null;
    	if(bAdmin) {
    		response = mChatRoomService.findRoom(req.getPageNumber(), req.getPageSize());
    	} else {
    		response = mChatRoomService.findJoinedRoom(userId, req.getPageNumber(), req.getPageSize());
    	}
    	
    	if(response!=null) {
    		return ResponseEntity.ok(response);   		
		} else {
			return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_NO_ROOM_EXIST, ErrorCodeEnum.CODE_NO_ROOM_EXIST.toString()));
		}
    	/*
    	GetRoomListResponse response = new GetRoomListResponse();
    	List<ChatRoom> rooms = new ArrayList<ChatRoom>();
    	if(bAdmin) {
	        List<ChatRoomDao> roomDaos = mChatRoomService.findRoom();
	        roomDaos.forEach(roomDao -> {
	        	ChatRoom roomResponse = new ChatRoom(roomDao.getRoomId(), roomDao.getRoomName(), roomDao.getRoomDescription(), 0);
	        	rooms.add(roomResponse);
	        });
	        response.setRoomList(rooms);
    	} else {
	        List<ChatRoomDao> roomDaos = 
	        roomDaos.forEach(roomDao -> {
	        	int unreadCnt = mChatMessageService.getUnreadMessageCnt(roomDao.getRoomId(), userId);
	        	ChatRoom roomResponse = new ChatRoom(roomDao.getRoomId(), roomDao.getRoomName(), roomDao.getRoomDescription(), unreadCnt);
	        	rooms.add(roomResponse);
	        });
	        response.setRoomList(rooms);
    	}
    	return ResponseEntity.ok(response);
    	*/
    }

    @PostMapping("/room/create")
    @ResponseBody
    public ResponseEntity<?> create(@RequestHeader (name="Authorization") String token, @RequestBody CreateRoomRequest req) {
    	if(req.getRoomName()==null || req.getRoomName().isEmpty() || req.getRoomDescription()==null || req.getRoomDescription().isEmpty()) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_INVALID_REQUEST, ErrorCodeEnum.CODE_INVALID_REQUEST.toString()));
    	}
    	
    	String jwtToken = JWTTokenParser.parse(token);
    	String userId = mJWTTokenProvider.getUserNameFromJwt(jwtToken);
    	ChatRoomDao roomDao = mChatRoomService.createRoom(req.getRoomName(), req.getRoomDescription(), userId, req.getRoomUsers());
    	
    	CreateRoomResponse response = new CreateRoomResponse();
    	response.setRoomId(roomDao.getRoomId());
    	response.setRoomName(roomDao.getRoomName());
    	response.setRoomDescription(roomDao.getRoomDescription());
    	
    	return ResponseEntity.ok(response);
    }
    
    @PostMapping("/room/delete")
    @ResponseBody
    public ResponseEntity<?> delete(@RequestHeader (name="Authorization") String token, @RequestBody DeleteRoomRequest req) {
    	if(req.getRoomId()==null || req.getRoomId().isEmpty()) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_INVALID_REQUEST, ErrorCodeEnum.CODE_INVALID_REQUEST.toString()));
    	}
    	
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
    	
    	if(bAdmin) {
    		mChatRoomService.deleteRoom(req.getRoomId(), userId);
    		return ResponseEntity.ok(new DeleteRoomResponse());
    	} else {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_DELETE_ROOM_IS_ALLOWED_ADMIN_ONLY, ErrorCodeEnum.CODE_DELETE_ROOM_IS_ALLOWED_ADMIN_ONLY.toString()));
    	}
    }
    
    @PostMapping("/room/join")
    @ResponseBody
    public ResponseEntity<?> join(@RequestHeader (name="Authorization") String token, @RequestBody JoinRoomRequest req) {
    	if(req.getRoomId()==null || req.getRoomId().isEmpty()) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_INVALID_REQUEST, ErrorCodeEnum.CODE_INVALID_REQUEST.toString()));
    	}
    	
    	String jwtToken = JWTTokenParser.parse(token);
    	String userId = mJWTTokenProvider.getUserNameFromJwt(jwtToken);
    	ErrorCodeEnum code = mChatRoomService.joinRoom(userId, req.getRoomId());
    	if(code==ErrorCodeEnum.CODE_SUCCESS) {
    		return ResponseEntity.ok(new SuccessResponse("join success"));
    	} else {
    		return ResponseEntity.ok(new ErrorResponse(code, code.toString()));
    	}
    }
    
    @PostMapping("/room/invite")
    @ResponseBody
    public ResponseEntity<?> invite(@RequestBody InviteUserToRoomRequest req) {
    	if(req.getUserId()==null || req.getUserId().isEmpty() || req.getRoomId()==null || req.getRoomId().isEmpty()) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_INVALID_REQUEST, ErrorCodeEnum.CODE_INVALID_REQUEST.toString()));
    	}
    	
    	boolean existUser = mChatUserService.existsByUserId(req.getUserId());
    	boolean existRoom = mChatRoomService.existRoomByRoomId(req.getRoomId());
    	if(existUser) {
        	boolean exist = mChatRoomService.existUserInRoom(req.getUserId(), req.getRoomId());
        	if(!exist) {
    	    	mChatRoomService.joinRoom(req.getUserId(), req.getRoomId());
    	    	return ResponseEntity.ok(new InviteUserToRoomResponse());
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
    public ResponseEntity<?> leave(@RequestHeader (name="Authorization") String token, @RequestBody LeaveUserFromRoomRequest req) {
    	if(req.getRoomId()==null || req.getRoomId().isEmpty()) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_INVALID_REQUEST, ErrorCodeEnum.CODE_INVALID_REQUEST.toString()));
    	}
    	
    	String jwtToken = JWTTokenParser.parse(token);
    	String userId = mJWTTokenProvider.getUserNameFromJwt(jwtToken);
    	
    	ChatRoomDao room = mChatRoomService.findRoomByRoomId(req.getRoomId());
    	if(room==null) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_ROOM_NOT_EXIST, ErrorCodeEnum.CODE_ROOM_NOT_EXIST.toString()));
    	}
    	
    	mChatRoomService.leaveRoom(userId, req.getRoomId());
    	return ResponseEntity.ok(new LeaveUserFromRoomResponse());
    }
    
    @PostMapping("/room/notify")
    @ResponseBody
    public ResponseEntity<?> notify(@RequestHeader (name="Authorization") String token, @RequestBody NotifyRoomMessageRequest req) {
    	if(req.getRoomIds()==null || req.getRoomIds().isEmpty() || req.getNType()==null || req.getRoomNotification()==null || req.getRoomNotification().isEmpty()) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_INVALID_REQUEST, ErrorCodeEnum.CODE_INVALID_REQUEST.toString()));
    	}
    	
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
    			mChatRoomService.notifyRoomMessage(roomId, req.getNType(), req.getRoomNotification());
    		});
    		return ResponseEntity.ok(new NotifyRoomMessageResponse());
    	} else {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_NOTIFY_MESSAGE_TO_ROOM_IS_ALLOWED_ADMIN_ONLY, ErrorCodeEnum.CODE_NOTIFY_MESSAGE_TO_ROOM_IS_ALLOWED_ADMIN_ONLY.toString()));
    	}
    }
    
    @PostMapping("/room/notifies")
    @ResponseBody
    public ResponseEntity<?> notifies(@RequestHeader (name="Authorization") String token, @RequestBody GetRoomNotifyListRequest req) {
    	
    	if(req.getRoomId()==null || req.getRoomId().isEmpty()) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_INVALID_REQUEST, ErrorCodeEnum.CODE_INVALID_REQUEST.toString()));
    	}
    	
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
    		List<ChatMessage> notifyList = mChatRoomService.getNotifyList(req.getRoomId());
    		GetRoomNotifyListResponse response = new GetRoomNotifyListResponse();
    		response.setNotifyList(notifyList);
    		return ResponseEntity.ok(response);
    	} else {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_GET_NOTIFY_MESSAGE_FROM_ROOM_IS_ALLOWED_ADMIN_ONLY, ErrorCodeEnum.CODE_GET_NOTIFY_MESSAGE_FROM_ROOM_IS_ALLOWED_ADMIN_ONLY.toString()));
    	}
    }
    
    @PostMapping("/room/section/delete")
    @ResponseBody
    public ResponseEntity<?> section_delete(@RequestHeader (name="Authorization") String token, @RequestBody DeleteSectionRequest req) {
    	
    	if(req.getRoomId()==null || req.getRoomId().isEmpty() || req.getMessageId()==null || req.getMessageId().isEmpty()) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_INVALID_REQUEST, ErrorCodeEnum.CODE_INVALID_REQUEST.toString()));
    	}
    	
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
    		ErrorCodeEnum code = mChatRoomService.deleteSection(req.getRoomId(), req.getMessageId());
    		if(code==ErrorCodeEnum.CODE_SUCCESS) {
    			return ResponseEntity.ok(new DeleteSectionResponse());
    		} else {
    			return ResponseEntity.ok(new ErrorResponse(code, code.toString()));
    		}
    	} else {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_DELETE_SECION_MESSAGE_FROM_ROOM_IS_ALLOWED_ADMIN_ONLY, ErrorCodeEnum.CODE_DELETE_SECION_MESSAGE_FROM_ROOM_IS_ALLOWED_ADMIN_ONLY.toString()));
    	}
    }
    
    
    @PostMapping("/room/users")
    @ResponseBody
    public ResponseEntity<?> users(@RequestBody GetRoomUserListRequest req) {
    	ChatRoomDao room = mChatRoomService.findRoomByRoomId(req.getRoomId());
    	if(room==null) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_ROOM_NOT_EXIST, ErrorCodeEnum.CODE_ROOM_NOT_EXIST.toString()));
    	}
    	
    	GetRoomUserListResponse response = mChatRoomService.getRoomUsers(req.getRoomId());
    	if(response!=null) {
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
    		return ResponseEntity.ok(new ChatRoom(room.getRoomId(), room.getRoomName(), room.getRoomDescription(), 0));
    	}
    }
    
    @PostMapping("/room/update")
    @ResponseBody
    public ResponseEntity<?> update(@RequestBody UpdateRoomRequest req) {
    	if(req.getRoomId()==null || req.getRoomId().isEmpty()) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_INVALID_REQUEST, ErrorCodeEnum.CODE_INVALID_REQUEST.toString()));
    	}
    	
    	if((req.getRoomName()==null || req.getRoomName().isEmpty()) && (req.getRoomDescription()==null || req.getRoomDescription().isEmpty())) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_INVALID_REQUEST, ErrorCodeEnum.CODE_INVALID_REQUEST.toString()));
    	}
    	
    	ChatRoomDao room = mChatRoomService.findRoomByRoomId(req.getRoomId());
    	if(room==null) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_ROOM_NOT_EXIST, ErrorCodeEnum.CODE_ROOM_NOT_EXIST.toString()));
    	}
    	
    	if(req.getRoomName()!=null && !req.getRoomName().isEmpty()) {
    		room.setRoomName(req.getRoomName());
    	}

    	if(req.getRoomDescription()!=null && !req.getRoomDescription().isEmpty()) {
    		room.setRoomDescription(req.getRoomDescription());
    	}
    	
    	return ResponseEntity.ok(new UpdateRoomResponse(room.getRoomId(), room.getRoomName(), room.getRoomDescription()));
    }
}