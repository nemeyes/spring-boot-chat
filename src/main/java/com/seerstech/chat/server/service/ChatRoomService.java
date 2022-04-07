package com.seerstech.chat.server.service;

import com.seerstech.chat.server.constant.ChatMessageEnum;
import com.seerstech.chat.server.constant.ChatNotificationEnum;
import com.seerstech.chat.server.constant.ErrorCodeEnum;
import com.seerstech.chat.server.model.ChatMessageDao;
import com.seerstech.chat.server.model.ChatRoomUserDao;
import com.seerstech.chat.server.model.ChatRoomDao;
import com.seerstech.chat.server.model.ChatUserDao;
import com.seerstech.chat.server.repo.ChatRoomUserRepository;
import com.seerstech.chat.server.repo.ChatMessageRepository;
import com.seerstech.chat.server.repo.ChatMessageStatusRepository;
import com.seerstech.chat.server.repo.ChatRoomRepository;
import com.seerstech.chat.server.repo.ChatUserRepository;
import com.seerstech.chat.server.utils.TimeUtil;
import com.seerstech.chat.server.vo.ChatMessage;
import com.seerstech.chat.server.vo.ChatRoom;
import com.seerstech.chat.server.vo.ChatRoomUser;
import com.seerstech.chat.server.vo.GetRoomListResponse;
import com.seerstech.chat.server.vo.GetRoomMessageListResponse;
import com.seerstech.chat.server.vo.GetRoomUserListResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ChatRoomService {

    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> mhoJoinedLiveParticipants;

    @Autowired
    private ChatUserRepository mChatUserRepository;
    @Autowired
    private ChatRoomRepository mChatRoomRepository;
    @Autowired
    private ChatRoomUserRepository mChatRoomUserRepository;
    @Autowired
    private ChatMessageRepository mChatMessageRepository;
    @Autowired
    private ChatMessageStatusRepository mChatMessageStatusRepository;
    @Autowired
    private ChatMessageService mChatMessageService;

    public ChatRoomDao findRoomByRoomId(String roomId) {
    	return mChatRoomRepository.findByRoomId(roomId);
    }
    
    public boolean existRoomByRoomId(String roomId) {
    	return mChatRoomRepository.existsByRoomId(roomId);
    }

    @Transactional
    public ChatRoomDao createRoom(String name, String description, String userId, List<String> participants) {

    	long ts = TimeUtil.unixTime();
        
        ChatRoomDao room = new ChatRoomDao();
        room.setRoomId(UUID.randomUUID().toString());
        room.setRoomName(name);
        room.setRoomDescription(description);
        room.setCreatedTime(ts);
        mChatRoomRepository.save(room);

        ChatUserDao user = mChatUserRepository.findByUserId(userId);
        ChatRoomUserDao creator = new ChatRoomUserDao();
        creator.setRoomId(room.getRoomId());
        creator.setUserId(user.getUserId());
        creator.setUserJoinedRoom(true);
        creator.setCreatedTime(ts);
        mChatRoomUserRepository.save(creator);
        
        ArrayList<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
        
        ChatMessage creatorMessage = ChatMessage.builder()
        								.messageId(UUID.randomUUID().toString())
        								.messageType(ChatMessageEnum.MSG_ENTER)
        								.roomId(room.getRoomId())
        								.userId(userId)
    									.createdTime(TimeUtil.unixTime())
        								.build();
        creatorMessage.setUserInfo(ChatRoomUser.builder()
        							.roomId(room.getRoomId())
        							.userId(user.getUserId())
        							.userNickname(user.getUserNickname())
        							.userJoinedRoom(true)
        							.build());
       
        chatMessages.add(creatorMessage);
 
        
        /*
        ArrayList<ChatUser> messageParticipants = new ArrayList<ChatUser>();
        messageParticipants.add(ChatUser.builder().userId(user.getUserId()).userNickname(user.getUserNickname()).build());
        */
        
        List<ChatUserDao> joiners = mChatUserRepository.findByUserIdIn(participants);
        joiners.forEach(item -> {
        	ChatRoomUserDao joiner = new ChatRoomUserDao();
        	joiner.setRoomId(room.getRoomId());
        	joiner.setUserId(item.getUserId());
        	joiner.setUserJoinedRoom(true);
        	joiner.setCreatedTime(ts);
        	mChatRoomUserRepository.save(joiner);
        	
        	ChatMessage joinerMessage = ChatMessage.builder()
        									.messageId(UUID.randomUUID().toString())
        									.messageType(ChatMessageEnum.MSG_ENTER)
        									.roomId(room.getRoomId())
        									.userId(item.getUserId())
        									.createdTime(TimeUtil.unixTime())
        									.build();
        	joinerMessage.setUserInfo(ChatRoomUser.builder()
        								.roomId(room.getRoomId())
        								.userId(item.getUserId())
        								.userNickname(item.getUserNickname())
        								.userJoinedRoom(true)
        								.build());
            chatMessages.add(joinerMessage);
            
        	//messageParticipants.add(ChatUser.builder().userId(item.getUserId()).userNickname(item.getUserNickname()).build());
        });
        
        chatMessages.forEach(chatMessage -> {
        	//chatMessage.setParticipants(messageParticipants);
        	mChatMessageService.send(chatMessage);
        });
        
        return room;
    }
    
    @Transactional
    public void deleteRoom(String roomId, String userId) {
        
    	ChatMessage roomDeleteMessage = ChatMessage.builder()
				.messageId(UUID.randomUUID().toString())
				.messageType(ChatMessageEnum.MSG_ROOM_DELETE)
				.roomId(roomId)
				.userId(userId)
				.createdTime(TimeUtil.unixTime())
				.build();
        
    	mChatMessageService.send(roomDeleteMessage);
    	
    	mChatMessageStatusRepository.deleteByRoomId(roomId);
    	mChatMessageRepository.deleteByRoomId(roomId);
    	mChatRoomUserRepository.deleteByRoomId(roomId);
    	mChatRoomRepository.deleteByRoomId(roomId);
    }
    
    public ErrorCodeEnum joinRoom(String userId, String roomId) {
    	
    	ChatRoomUserDao chatRoomUser = mChatRoomUserRepository.findByRoomIdAndUserId(roomId, userId);
    	if(chatRoomUser!=null) {
    		if(chatRoomUser.getUserJoinedRoom()==true) {
    			return ErrorCodeEnum.CODE_USER_ALREADY_EXIST_IN_ROOM;
    		} else {
    			chatRoomUser.setUserJoinedRoom(true);
    			mChatRoomUserRepository.save(chatRoomUser);
    			
                ChatMessage joinerMessage = ChatMessage.builder()
						.messageId(UUID.randomUUID().toString())
						.messageType(ChatMessageEnum.MSG_ENTER)
						.roomId(roomId)
						.userId(userId)
						.createdTime(TimeUtil.unixTime())
						.build();
                
                ChatUserDao userInfo = mChatUserRepository.findByUserId(userId);
                
				joinerMessage.setUserInfo(ChatRoomUser.builder()
									.roomId(chatRoomUser.getRoomId())
									.userId(userInfo.getUserId())
									.userNickname(userInfo.getUserNickname())
									.userJoinedRoom(true)
									.build());
				mChatMessageService.send(joinerMessage);
    			return ErrorCodeEnum.CODE_SUCCESS;
    		}
    	} else {
            ChatUserDao user = mChatUserRepository.findByUserId(userId);
        	//ChatRoomDao room = mChatRoomRepository.findByRoomId(roomId);
        	
        	long ts = TimeUtil.unixTime();
            ChatRoomUserDao joiner = new ChatRoomUserDao();
            joiner.setRoomId(roomId);
            joiner.setUserId(userId);
            joiner.setUserJoinedRoom(true);
            joiner.setCreatedTime(ts);
            mChatRoomUserRepository.save(joiner);
            
            ChatMessage joinerMessage = ChatMessage.builder()
            								.messageId(UUID.randomUUID().toString())
            								.messageType(ChatMessageEnum.MSG_ENTER)
            								.roomId(roomId)
            								.userId(userId)
        									.createdTime(TimeUtil.unixTime())
            								.build();
            joinerMessage.setUserInfo(ChatRoomUser.builder()
            							.roomId(roomId)
            							.userId(user.getUserId())
            							.userNickname(user.getUserNickname())
            							.userJoinedRoom(true)
            							.build());
            mChatMessageService.send(joinerMessage);
            return ErrorCodeEnum.CODE_SUCCESS;
    	}
    }
    
    /*
    public List<ChatRoomDao> findJoinedRoom(String userId) {

    	//ChatUserDao user = mChatUserRepository.findByUserId(userId);
    	//List<ChatParticipantDao> participants = mChatParticipantRepository.findByUserId(user.getId());
    	List<ChatRoomUserDao> participants = mChatRoomUserRepository.findByUserIdAndUserJoinedRoom(userId, true);
    	
    	List<ChatRoomDao> rooms = new ArrayList<ChatRoomDao>();
    	participants.forEach(participant -> {
    		ChatRoomDao room = mChatRoomRepository.findByRoomId(participant.getRoomId());
    		
    		rooms.add(room);
    	});
    	return rooms;
    }
    */
    
    public GetRoomListResponse findRoom(int page, int size) {
    	try {
    		GetRoomListResponse response = new GetRoomListResponse();
        	List<ChatRoom> rooms = new ArrayList<ChatRoom>();
    		Pageable paging = PageRequest.of(page-1,  size);
    		Page<ChatRoomDao> pageRoomList =  mChatRoomRepository.findAll(paging);
    		List<ChatRoomDao> roomDaoList = pageRoomList.getContent();
    		
    		roomDaoList.forEach(roomDao->{
    			ChatRoom room = new ChatRoom(roomDao.getRoomId(), roomDao.getRoomName(), roomDao.getRoomDescription(), 0);
    			rooms.add(room);
    		});
    		
    		response.setRoomList(rooms);
			response.setCurrentPage(pageRoomList.getNumber() + 1);
			response.setTotalPages(pageRoomList.getTotalPages());
			response.setTotalCount(pageRoomList.getTotalElements());
        	
			return response;
    		
    	} catch(Exception e) {
    		return null;
    	}
    }
    
    public GetRoomListResponse findJoinedRoom(String userId, int page, int size) {

    	try {
    		GetRoomListResponse response = new GetRoomListResponse();
        	List<ChatRoom> rooms = new ArrayList<ChatRoom>();
        	
        	//List<ChatRoomUserDao> joinedRoomList = null;
    		Pageable paging = PageRequest.of(page-1,  size);
    		Page<ChatRoomUserDao> pageRoomUserList  = mChatRoomUserRepository.findByUserIdAndUserJoinedRoom(userId, true, paging);
    		List<ChatRoomUserDao> joinedRoomList = pageRoomUserList.getContent();
    		
    		joinedRoomList.forEach(participant -> {
        		ChatRoomDao roomDao = mChatRoomRepository.findByRoomId(participant.getRoomId());
        		int unreadCnt = mChatMessageService.getUnreadMessageCnt(roomDao.getRoomId(), userId);
        		ChatRoom room = new ChatRoom(roomDao.getRoomId(), roomDao.getRoomName(), roomDao.getRoomDescription(), unreadCnt);
        		rooms.add(room);
        	});
    		
    		response.setRoomList(rooms);
			response.setCurrentPage(pageRoomUserList.getNumber() + 1);
			response.setTotalPages(pageRoomUserList.getTotalPages());
			response.setTotalCount(pageRoomUserList.getTotalElements());
			
			return response;
    		
    	} catch(Exception e) {
    		return null;
    	}	
    }
    
    public GetRoomUserListResponse getRoomUsers(String roomId) {
    	
    	List<ChatRoomUserDao> roomUserDaos = mChatRoomUserRepository.findByRoomId(roomId);
    	if(roomUserDaos==null) {
    		return null;
    	}
    	
    	GetRoomUserListResponse response = new GetRoomUserListResponse();
    	List<ChatRoomUser> roomUsers = new ArrayList<ChatRoomUser>();
    
    	roomUserDaos.forEach(roomUserDao-> {
    		ChatUserDao item = mChatUserRepository.findByUserId(roomUserDao.getUserId());
    		if(item!=null) {
    			ChatRoomUser roomUser = ChatRoomUser.builder()
    										.roomId(roomId)
    										.userId(item.getUserId())
    										.userNickname(item.getUserNickname())
    										.userJoinedRoom(roomUserDao.getUserJoinedRoom())
    										.build();
    			roomUsers.add(roomUser);
    		}
    	});
    	
    	response.setRoomId(roomId);
    	response.setRoomUsers(roomUsers);
    	return response;
    }
    
    
    public boolean existUserInRoom(String userId, String roomId) {
    	
    	Boolean exist = mChatRoomUserRepository.existsByRoomIdAndUserIdAndUserJoinedRoom(roomId, userId, true);
    	return exist;
    	
    	/*
        ChatUserDao user = mChatUserRepository.findByUserId(userId);
    	ChatRoomDao room = mChatRoomRepository.findByRoomId(roomId);
    	if(user!=null && room!=null) {
    		return mChatParticipantRepository.existsByRoomIdAndUserId(room.getId(), user.getId());
    	} else {
    		return false;
    	}
    	*/
    }
    
    public void leaveRoom(String userId, String roomId) {
    	
    	ChatUserDao user = mChatUserRepository.findByUserId(userId);
    	//ChatRoomDao room = mChatRoomRepository.findByRoomId(roomId);
    	
    	
    	ChatRoomUserDao dao = mChatRoomUserRepository.findByRoomIdAndUserId(roomId, userId);
    	dao.setUserJoinedRoom(false);
    	dao.setDeletedTime(TimeUtil.unixTime());
    	mChatRoomUserRepository.save(dao);
    
        ChatMessage leaveMessage = ChatMessage.builder()
        								.messageId(UUID.randomUUID().toString())
        								.messageType(ChatMessageEnum.MSG_QUIT)
        								.roomId(roomId)
        								.userId(userId)
    									.createdTime(TimeUtil.unixTime())
        								.build();
        leaveMessage.setUserInfo(ChatRoomUser.builder()
        							.roomId(roomId)
        							.userId(user.getUserId())
        							.userNickname(user.getUserNickname())
        							.userJoinedRoom(false)
        							.build());
        
        mChatMessageService.send(leaveMessage);
		
        /*
    	long count = mChatParticipantRepository.countByRoomId(room.getId());
    	if(count==0) {
    		mChatRoomRepository.delete(room); //delete room from mongodb
    	}
    	*/
    }

    public void notifyRoomMessage(String roomId, ChatNotificationEnum nType, String roomMessage) {
        ChatMessage notiMessage = ChatMessage.builder()
        							.messageId(UUID.randomUUID().toString())
        							.messageType(ChatMessageEnum.MSG_NOTI)
        							.roomId(roomId)
        							.message(roomMessage)
									.createdTime(TimeUtil.unixTime())
        							.build();
        notiMessage.setNType(nType);
        mChatMessageService.send(notiMessage);
    }
    
    public List<ChatMessage> getNotifyList(String roomId) {
    	List<ChatMessage> notifyList = new ArrayList<ChatMessage>();
    	
    	
    	/*
		//Criteria criteria = new Criteria();
		Criteria criteria = Criteria.where("created_time").gt("deleted_time");
		criteria = criteria.and("type").is(ChatMessageEnum.MSG_NOTI);
		criteria = criteria.and("room_id").is(roomId);
		Query query = new Query(criteria);
		
		ArrayList<String> messageIds = new ArrayList<String>();
		DistinctIterable<String> messageIdsIter = mongoTemplate.getCollection("ChatMessageStatus").distinct("message_id", query.getQueryObject(), String.class);
		MongoCursor<?> cursor = messageIdsIter.iterator();
		while ( cursor.hasNext() ) {
			String messageId = (String)cursor.next();
			messageIds.add(messageId);
		}
		*/
    	
    	
    	List<ChatMessageDao> notifyDaoList = mChatMessageRepository.findByRoomIdAndTypeAndDeletedTime(roomId, ChatMessageEnum.MSG_NOTI, 0L);
    	notifyDaoList.forEach(notifyDao->{
            ChatMessage notifyMessage = ChatMessage.builder()
					.messageId(notifyDao.getMessageId())
					.messageType(notifyDao.getType())
					.message(notifyDao.getMessage())
					.roomId(roomId)
					.userId(notifyDao.getUserId())
					.createdTime(TimeUtil.unixTime())
					.build();
            
            notifyMessage.setNType(notifyDao.getNType());
    		notifyList.add(notifyMessage);
    	});
    	
    	return notifyList;
    }
    
    public ErrorCodeEnum deleteSection(String roomId, String messageId) {
    	
    	ChatMessageDao startSectionMessage = mChatMessageRepository.findByMessageId(messageId);
    	if(startSectionMessage.getType()!=ChatMessageEnum.MSG_NOTI || startSectionMessage.getNType()!=ChatNotificationEnum.NOTI_START_SECTION) {
    		return ErrorCodeEnum.CODE_DELETE_SECTION_REQUIRE_START_SECTION_MESSAGE;
    	}
    	
    	List<ChatMessageDao> positiveNearestEndSectionDao = mChatMessageRepository.findNearestMessage(roomId, ChatMessageEnum.MSG_NOTI, ChatNotificationEnum.NOTI_END_SECTION, startSectionMessage.getCreatedTime(), PageRequest.of(0,  1));
    	if(positiveNearestEndSectionDao.isEmpty()) {
    		return ErrorCodeEnum.CODE_DELETE_SECTION_HAS_END_SECTION_ALONG_WITH_START_SECTION;
    	}
    	
    	long startTime = startSectionMessage.getCreatedTime();
    	long endTime = positiveNearestEndSectionDao.get(0).getCreatedTime();
    	List<ChatMessageDao> messageToBeDeletedList = mChatMessageRepository.findScopedMessageByRoomId(roomId, startTime, endTime);
    	messageToBeDeletedList.forEach(messageToBeDeleted->{

    		if((messageToBeDeleted.getType()==ChatMessageEnum.MSG_TALK) || 
    		   (messageToBeDeleted.getType()==ChatMessageEnum.MSG_FILE)) {
    			
    			mChatMessageService.markAsDelete(messageToBeDeleted.getMessageId(), messageToBeDeleted.getUserId());
    			
    		} else {
        		messageToBeDeleted.setDeletedTime(TimeUtil.unixTime());
        		mChatMessageRepository.save(messageToBeDeleted);
    			
    	        ChatMessage markAsDeleteMessage = ChatMessage.builder()
    					.messageId(messageToBeDeleted.getMessageId())
    					.messageType(ChatMessageEnum.MSG_DELETE)
    					.roomId(messageToBeDeleted.getRoomId())
    					.userId(messageToBeDeleted.getUserId())
    					.createdTime(messageToBeDeleted.getCreatedTime())
    					.build();
    	        
    	        mChatMessageService.pureSend(markAsDeleteMessage);
    		}
    	});
    	return ErrorCodeEnum.CODE_SUCCESS;
    }
    
	public void saveChatRoom(ChatRoomDao room) {
		mChatRoomRepository.save(room);
	}
	
    
    public void joinActiveRoom(String sessionId, String roomId) {
        mhoJoinedLiveParticipants.append(sessionId, roomId);
    }

    public String getJoinedActiveRoom(String sessionId) {
        return mhoJoinedLiveParticipants.get(sessionId);
    }

    public void leaveActiveRoom(String sessionId) {
        mhoJoinedLiveParticipants.getAndDelete(sessionId);
    }

    /*
    public long getUserCount(String roomId) {
        return Long.valueOf(Optional.ofNullable(mvoJoinedUserCount.get(roomId)).orElse("0"));
    }

    public long plusUserCount(String roomId) {
        return Optional.ofNullable(mvoJoinedUserCount.increment(roomId)).orElse(0L);
    }

    public long minusUserCount(String roomId) {
        return Optional.ofNullable(mvoJoinedUserCount.decrement(roomId)).filter(count -> count > 0).orElse(0L);
    }
    
    public void deleteUserCount(String roomId) {
    	Optional.ofNullable(mvoJoinedUserCount.getAndDelete(roomId));
    }
    */
}