package com.seerstech.chat.server.service;

import com.seerstech.chat.server.model.ChatMessageEnum;
import com.seerstech.chat.server.model.ChatParticipantDao;
import com.seerstech.chat.server.model.ChatRoomDao;
import com.seerstech.chat.server.model.ChatUserDao;
import com.seerstech.chat.server.repo.ChatParticipantRepository;
import com.seerstech.chat.server.repo.ChatRoomRepository;
import com.seerstech.chat.server.repo.ChatUserRepository;
import com.seerstech.chat.server.utils.TimeUtil;
import com.seerstech.chat.server.vo.ChatMessage;
import com.seerstech.chat.server.vo.ChatUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private ChatParticipantRepository mChatParticipantRepository;
    @Autowired
    private ChatMessageService mChatMessageService;

    public ChatRoomDao findRoomByRoomId(String roomId) {
    	return mChatRoomRepository.findByRoomId(roomId);
    }
    
    public boolean existRoomByRoomId(String roomId) {
    	return mChatRoomRepository.existsByRoomId(roomId);
    }
    
    public List<ChatRoomDao> findRoom() {
    	return mChatRoomRepository.findAll();
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
        ChatParticipantDao creator = new ChatParticipantDao();
        creator.setRoomId(room.getId());
        creator.setUserId(user.getId());
        creator.setCreatedTime(ts);
        mChatParticipantRepository.save(creator);
        
        ArrayList<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
        
        ChatMessage creatorMessage = ChatMessage.builder()
        								.messageId(UUID.randomUUID().toString())
        								.messageType(ChatMessageEnum.MSG_ENTER)
        								.roomId(room.getRoomId())
        								.userId(userId)
    									.createdTime(TimeUtil.unixTime())
        								.build();
        chatMessages.add(creatorMessage);
        
        ArrayList<ChatUser> messageParticipants = new ArrayList<ChatUser>();
        messageParticipants.add(ChatUser.builder().userId(user.getUserId()).userNickname(user.getUserNickname()).build());
        
        List<ChatUserDao> joiners = mChatUserRepository.findByUserIdIn(participants);
        joiners.forEach(item -> {
        	ChatParticipantDao joiner = new ChatParticipantDao();
        	joiner.setRoomId(room.getId());
        	joiner.setUserId(item.getId());
        	joiner.setCreatedTime(ts);
        	mChatParticipantRepository.save(joiner);
        	
        	ChatMessage joinerMessage = ChatMessage.builder()
        									.messageId(UUID.randomUUID().toString())
        									.messageType(ChatMessageEnum.MSG_ENTER)
        									.roomId(room.getRoomId())
        									.userId(item.getUserId())
        									.createdTime(TimeUtil.unixTime())
        									.build();
            chatMessages.add(joinerMessage);
            
        	messageParticipants.add(ChatUser.builder().userId(item.getUserId()).userNickname(item.getUserNickname()).build());
        });
        
        chatMessages.forEach(chatMessage -> {
        	chatMessage.setParticipants(messageParticipants);
        	mChatMessageService.sendChatMessage(chatMessage);
        });
        
        return room;
    }
    
    public void joinRoom(String userId, String roomId) {

        ChatUserDao user = mChatUserRepository.findByUserId(userId);
    	ChatRoomDao room = mChatRoomRepository.findByRoomId(roomId);
    	
    	long ts = TimeUtil.unixTime();
        ChatParticipantDao joiner = new ChatParticipantDao();
        joiner.setRoomId(room.getId());
        joiner.setUserId(user.getId());
        joiner.setCreatedTime(ts);
        mChatParticipantRepository.save(joiner);
        
        ArrayList<ChatUser> messageParticipants = new ArrayList<ChatUser>();
        List<ChatUserDao> users = getRoomUsers(roomId);
        users.forEach(item -> {
        	messageParticipants.add(ChatUser.builder().userId(item.getUserId()).userNickname(item.getUserNickname()).build());
        });
        
        ChatMessage joinerMessage = ChatMessage.builder()
        								.messageId(UUID.randomUUID().toString())
        								.messageType(ChatMessageEnum.MSG_ENTER)
        								.roomId(roomId)
        								.userId(userId)
    									.createdTime(TimeUtil.unixTime())
        								.build();
        joinerMessage.setParticipants(messageParticipants);
        mChatMessageService.sendChatMessage(joinerMessage);
        
        //plusUserCount(roomId);
    }
    
    public List<ChatRoomDao> findJoinedRoom(String userId) {

    	ChatUserDao user = mChatUserRepository.findByUserId(userId);
    	List<ChatParticipantDao> participants = mChatParticipantRepository.findByUserId(user.getId());
    	
    	List<ChatRoomDao> rooms = new ArrayList<ChatRoomDao>();
    	participants.forEach(participant -> {
    		Optional<ChatRoomDao> optional = mChatRoomRepository.findById(participant.getRoomId());
    		ChatRoomDao room = optional.get();
    		rooms.add(room);
    	});
    	return rooms;
    }
    
    public List<ChatUserDao> getRoomUsers(String roomId) {
    	ChatRoomDao room = mChatRoomRepository.findByRoomId(roomId);
    	List<ChatParticipantDao> particiants = mChatParticipantRepository.findByRoomId(room.getId());
    	List<ChatUserDao> users = new ArrayList<ChatUserDao>();
    	particiants.forEach(participant-> {
    		Optional<ChatUserDao> user = mChatUserRepository.findById(participant.getUserId());
    		if(user!=null && user.get()!=null) {
    			users.add(user.get());
    		}
    	});
    	return users;
    }
    
    public boolean existUserInRoom(String userId, String roomId) {
        ChatUserDao user = mChatUserRepository.findByUserId(userId);
    	ChatRoomDao room = mChatRoomRepository.findByRoomId(roomId);
    	if(user!=null && room!=null) {
    		return mChatParticipantRepository.existsByRoomIdAndUserId(room.getId(), user.getId());
    	} else {
    		return false;
    	}
    }
    
    public void leaveRoom(String userId, String roomId) {
    	
    	ChatUserDao user = mChatUserRepository.findByUserId(userId);
    	ChatRoomDao room = mChatRoomRepository.findByRoomId(roomId);
    	
		mChatParticipantRepository.deleteByRoomIdAndUserId(room.getId(), user.getId());
		
        ChatMessage leaveMessage = ChatMessage.builder()
        								.messageId(UUID.randomUUID().toString())
        								.messageType(ChatMessageEnum.MSG_QUIT)
        								.roomId(roomId)
        								.userId(userId)
    									.createdTime(TimeUtil.unixTime())
        								.build();
        mChatMessageService.sendChatMessage(leaveMessage);
		
    	long count = mChatParticipantRepository.countByRoomId(room.getId());
    	if(count==0) {
    		mChatRoomRepository.delete(room); //delete room from mongodb
    	}
    }

    public void notifyRoomMessage(String roomId, String roomMessage) {
        ChatMessage notiMessage = ChatMessage.builder()
        							.messageId(UUID.randomUUID().toString())
        							.messageType(ChatMessageEnum.MSG_NOTI)
        							.roomId(roomId)
        							.message(roomMessage)
									.createdTime(TimeUtil.unixTime())
        							.build();
        mChatMessageService.sendChatMessage(notiMessage);
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