package com.seerstech.chat.server.service;

import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;
import com.seerstech.chat.server.constant.ChatMessageEnum;
import com.seerstech.chat.server.constant.ErrorCodeEnum;
import com.seerstech.chat.server.exception.FileStorageException;
import com.seerstech.chat.server.model.ChatMessageStatusDao;
import com.seerstech.chat.server.model.ChatRoomUserDao;
import com.seerstech.chat.server.model.ChatUserDao;
import com.seerstech.chat.server.repo.ChatMessageStatusRepository;
import com.seerstech.chat.server.repo.ChatMessageRepository;
import com.seerstech.chat.server.repo.ChatRoomUserRepository;
import com.seerstech.chat.server.repo.ChatUserRepository;
import com.seerstech.chat.server.model.ChatMessageDao;
import com.seerstech.chat.server.vo.GetRoomMessageListResponse;
import com.seerstech.chat.server.utils.TimeUtil;
import com.seerstech.chat.server.vo.ChatMessage;
import com.seerstech.chat.server.vo.ChatRoomUser;
import com.seerstech.chat.server.vo.ErrorResponse;
import com.seerstech.chat.server.vo.UploadResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Service
public class ChatMessageService {
	
    @Autowired
    private ChatMessageRepository mChatMessageRepository;
	@Autowired
	private ChatUserRepository mChatUserRepository;
	@Autowired
	private ChatRoomUserRepository mChatRoomUserRepository;
	@Autowired
	private ChatMessageStatusRepository mChatMessageStatusRepository;
	
    private final ChannelTopic mChannelTopic;
    private final RedisTemplate<?, ?> mRedisTemplate;
        
    @Resource(name = "uploadDir")
    private final String mFileStorageLocation;
    
    @Resource(name = "downloadUrl")
    private final String mDownloadUrl;
    
    @Autowired
	private MongoTemplate mongoTemplate;

    
    @Autowired
    public ChatMessageService(ChannelTopic channelTopic, RedisTemplate<?, ?> redisTemplate, String uploadDir, String downloadUrl) {
		this.mChannelTopic = channelTopic;
		this.mRedisTemplate = redisTemplate;
		this.mFileStorageLocation = uploadDir;    
		this.mDownloadUrl = downloadUrl;
    }
    
    public String getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf('/');
        if (lastIndex != -1)
            return destination.substring(lastIndex + 1);
        else
            return "";
    }
    
    public GetRoomMessageListResponse getMessageListByRoomId(String roomId, int page, int size) {

    	try {
        	GetRoomMessageListResponse response = new GetRoomMessageListResponse();
        	
        	List<ChatMessageDao> pageMessageDaos = new ArrayList<ChatMessageDao>();
    		Pageable paging = PageRequest.of(page-1,  size);
    		Page<ChatMessageDao> pageMessageList = mChatMessageRepository.findByRoomId(roomId, paging);
    		
    		pageMessageDaos = pageMessageList.getContent();
    		List<ChatMessage> messageList = new ArrayList<ChatMessage>();
    		
    		pageMessageDaos.forEach(item -> {
    			ChatMessage message = ChatMessage.builder()
    					.messageId(item.getMessageId())
    					.messageType(item.getType())
    					.roomId(item.getRoomId())
    					.userId(item.getUserId())
    					.message(item.getMessage())
    					.mimeType(item.getMimeType())
    					.downloadPath(mDownloadUrl + item.getDownloadPath())
    					.createdTime(item.getCreatedTime())
    					.build();
    			message.setNType(item.getNType());
    			
    			if(item.getCreatedTime()<=item.getDeletedTime()) {
    				message.setType(ChatMessageEnum.MSG_DELETE);
    				message.setMessage("");
    				message.setDownloadPath("");
    				message.setMimeType("");
    			}
    			
    			if((message.getType()==ChatMessageEnum.MSG_ENTER) || (message.getType()==ChatMessageEnum.MSG_QUIT)) {
    				ChatUserDao userItem = mChatUserRepository.findByUserId(message.getUserId());
    				ChatRoomUser user = ChatRoomUser.builder()
    						.roomId(roomId)
    						.userId(userItem.getUserId())
    						.userNickname(userItem.getUserNickname())
    						.userJoinedRoom((message.getType()==ChatMessageEnum.MSG_ENTER)?true:false)
    						.build();
    				
    				message.setUserInfo(user);
    			}
    			
    			if(item.getParentMessageId()!=null && item.getParentMessageId().length()>0) {
    				ChatMessageDao parentItem = mChatMessageRepository.findByMessageId(item.getParentMessageId());
    				
    				ChatMessage parentMessage = ChatMessage.builder()
    						.messageId(parentItem.getMessageId())
    						.messageType(parentItem.getType())
    						.roomId(parentItem.getRoomId())
        					.userId(parentItem.getUserId())
        					.message(parentItem.getMessage())
        					.mimeType(parentItem.getMimeType())
        					.downloadPath(mDownloadUrl + parentItem.getDownloadPath())
        					.createdTime(parentItem.getCreatedTime())
        					.build();
    				
        			if(parentItem.getCreatedTime()<=parentItem.getDeletedTime()) {
        				parentMessage.setType(ChatMessageEnum.MSG_DELETE);
        				parentMessage.setMessage("");
        				parentMessage.setDownloadPath("");
        				parentMessage.setMimeType("");
        			}
    						
    				message.setParentMessageId(parentMessage.getMessageId());
    				message.setParentMessage(parentMessage);
    			}
    			
    			if(message.getType()==ChatMessageEnum.MSG_TALK || message.getType()==ChatMessageEnum.MSG_FILE) {
    				final List<String> unreadUserIdList = new ArrayList<String>();
    				List<ChatMessageStatusDao> unreadUserListDao = mChatMessageStatusRepository.findByRoomIdAndMessageId(roomId, message.getMessageId());
    				unreadUserListDao.forEach(unreadUserDao->{
    					unreadUserIdList.add(unreadUserDao.getUserId());
    				});
    				message.setUnreadUserIdList(unreadUserIdList);
    			}
    			
    			
    			
    			messageList.add(message);
            });
            
			response.setRoomId(roomId);
			response.setMessageList(messageList);
			response.setCurrentMessagePage(pageMessageList.getNumber() + 1);
			response.setTotalMessagePages(pageMessageList.getTotalPages());
			response.setTotalMessageCount(pageMessageList.getTotalElements());
			
        	return response;
    	} catch(Exception e) {
    		return null;
    	}
    }
    
    public ErrorCodeEnum markAsRead(String roomId, String userId, String messageId) {
    	
    	boolean exist = mChatMessageStatusRepository.existsByRoomIdAndUserIdAndMessageId(roomId, userId, messageId);
    	
    	if(exist) {
	    	mChatMessageStatusRepository.deleteByRoomIdAndUserIdAndMessageId(roomId, userId, messageId);
	    	
	    	final List<String> unreadUserIdList = new ArrayList<String>();
	    	List<ChatMessageStatusDao> messageStatusDaoList = mChatMessageStatusRepository.findByRoomIdAndMessageId(roomId, messageId);
	    	messageStatusDaoList.forEach(messageStatusDao -> {
		    	unreadUserIdList.add(messageStatusDao.getUserId());
	    	});
	    	
	    	ChatMessageDao messageDao = mChatMessageRepository.findByMessageId(messageId);
	    	
	        ChatMessage markAsReadMessage = ChatMessage.builder()
					.messageId(messageId)
					.messageType(ChatMessageEnum.MSG_READ)
					.roomId(roomId)
					.userId(messageDao.getUserId())
					.createdTime(messageDao.getCreatedTime())
					.build();
	        
	        markAsReadMessage.setUnreadUserIdList(unreadUserIdList);
	        mRedisTemplate.convertAndSend(mChannelTopic.getTopic(), markAsReadMessage);
	        
	    	return ErrorCodeEnum.CODE_SUCCESS;
    	} else {
    		return ErrorCodeEnum.CODE_READ_MARK_NOT_EXIST;
    	}
    }
    
    public ErrorCodeEnum markAsDelete(String messageId, String userId) {
    	
    	boolean exist = mChatMessageStatusRepository.existsByMessageId(messageId);
       	if(exist) {
	    	mChatMessageStatusRepository.deleteByMessageId(messageId);
       	}
    	
    	ChatMessageDao messageDao = mChatMessageRepository.findByMessageId(messageId);
    	
    	if(messageDao.getUserId().equals(userId)) {
    		messageDao.setDeletedTime(TimeUtil.unixTime());
    		mChatMessageRepository.save(messageDao);
    		
	        ChatMessage markAsDeleteMessage = ChatMessage.builder()
					.messageId(messageId)
					.messageType(ChatMessageEnum.MSG_DELETE)
					.roomId(messageDao.getRoomId())
					.userId(messageDao.getUserId())
					.createdTime(messageDao.getCreatedTime())
					.build();
	        mRedisTemplate.convertAndSend(mChannelTopic.getTopic(), markAsDeleteMessage);
	        
	        List<ChatMessageDao> childMessaeDaoList = mChatMessageRepository.findByParentMessageId(messageId);
	        childMessaeDaoList.forEach(childMessageDao->{
		        ChatMessage markAsDeleteParentMessage = ChatMessage.builder()
						.messageId(childMessageDao.getMessageId())
						.messageType(ChatMessageEnum.MSG_PARENT_DELETE)
						.roomId(messageDao.getRoomId())
						.userId(messageDao.getUserId())
						.createdTime(messageDao.getCreatedTime())
						.build();
		        markAsDeleteParentMessage.setParentMessageId(messageId);
		        mRedisTemplate.convertAndSend(mChannelTopic.getTopic(), markAsDeleteParentMessage);
	        });
    		
    		return ErrorCodeEnum.CODE_SUCCESS;
    	} else {
    		return ErrorCodeEnum.CODE_DELETE_MARK_NOT_ALLOWED;
    	}
    }
    
    public void pureSend(ChatMessage message) {
    	mRedisTemplate.convertAndSend(mChannelTopic.getTopic(), message);
    }
    
    public void send(ChatMessage message) {
    	
    	
    	//if parent message is deleted(mark as delete), sub message(new message) can not be saved and sended to participants
    	if(message.getParentMessageId()!=null && message.getParentMessageId().length()>0) {
    		ChatMessageDao parentMessageDao = mChatMessageRepository.findByMessageId(message.getParentMessageId());
    		if(parentMessageDao.getDeletedTime()>0) {
    			return;
    		}
    		
    		ChatMessage parentMessage = ChatMessage.builder()
											.messageId(parentMessageDao.getMessageId())
											.messageType(parentMessageDao.getType())
											.roomId(parentMessageDao.getRoomId())
											.userId(parentMessageDao.getUserId())
											.message(parentMessageDao.getMessage())
											.mimeType(parentMessageDao.getMimeType())
											.downloadPath(mDownloadUrl + parentMessageDao.getDownloadPath())
											.createdTime(parentMessageDao.getCreatedTime())
											.build();
    		
			message.setParentMessage(parentMessage);
    	}
    	
    	if(message.getType()==ChatMessageEnum.MSG_ROOM_DELETE) {
    		mRedisTemplate.convertAndSend(mChannelTopic.getTopic(), message);
    		return;
    	} 
    	
    	if(message.getType()==ChatMessageEnum.MSG_QUIT) {
    		
    		String roomId = message.getRoomId();
    		String userId = message.getUserId();
    		
    		Criteria criteria = new Criteria();
    		criteria = criteria.and("room_id").is(roomId);
    		criteria = criteria.and("user_id").is(userId);
    		Query query = new Query(criteria);
    		
    		ArrayList<String> messageIds = new ArrayList<String>();
    		DistinctIterable<String> messageIdsIter = mongoTemplate.getCollection("ChatMessageStatus").distinct("message_id", query.getQueryObject(), String.class);
    		MongoCursor<?> cursor = messageIdsIter.iterator();
    		while ( cursor.hasNext() ) {
    			String messageId = (String)cursor.next();
    			messageIds.add(messageId);
    		}
    		
    		//List<String> messageIds = mChatMessageStatusRepository.findDistinctMessageIdByRoomIdAndUserId(roomId, message.getUserId());
    		messageIds.forEach(messageId->{
    	    	
    			final List<String> unreadUserIdList = new ArrayList<String>();
    	    	List<ChatMessageStatusDao> messageStatusDaoList = mChatMessageStatusRepository.findByRoomIdAndMessageId(roomId, messageId);
    	    	messageStatusDaoList.forEach(messageStatusDao -> {
    	    		if(!userId.equals(messageStatusDao.getUserId())) {
    	    			unreadUserIdList.add(messageStatusDao.getUserId());
    	    		}
    	    	});
    	    	
    	    	ChatMessageDao messageDao = mChatMessageRepository.findByMessageId(messageId);
    	    	
    	        ChatMessage markAsReadMessage = ChatMessage.builder()
    					.messageId(messageId)
    					.messageType(ChatMessageEnum.MSG_READ)
    					.roomId(roomId)
    					.userId(messageDao.getUserId())
    					.createdTime(TimeUtil.unixTime())
    					.build();
    	        
    	        markAsReadMessage.setUnreadUserIdList(unreadUserIdList);
    	        mRedisTemplate.convertAndSend(mChannelTopic.getTopic(), markAsReadMessage);
    	        
    		});
    		
    		mChatMessageStatusRepository.deleteByRoomIdAndUserId(roomId, message.getUserId());
    	}
    	
    	final List<String> unreadUserIdList = new ArrayList<String>();
		if(message.getType()==ChatMessageEnum.MSG_TALK || message.getType()==ChatMessageEnum.MSG_FILE) {
	    	List<ChatRoomUserDao> roomUserDaoList = mChatRoomUserRepository.findByRoomId(message.getRoomId());
	    	roomUserDaoList.forEach(roomUserDao -> {
	    		if(!message.getUserId().equals(roomUserDao.getUserId()) && roomUserDao.getUserJoinedRoom()==true) {
		    		ChatMessageStatusDao messageReadDao = new ChatMessageStatusDao();
		    		messageReadDao.setRoomId(message.getRoomId());
		    		messageReadDao.setUserId(roomUserDao.getUserId());
		    		messageReadDao.setMessageId(message.getMessageId());
		    		mChatMessageStatusRepository.save(messageReadDao);
	
		    		unreadUserIdList.add(roomUserDao.getUserId());
	    		}
	    	});
		}
    	
    	if(message.getType()==ChatMessageEnum.MSG_TALK || message.getType()==ChatMessageEnum.MSG_FILE) {
    		message.setUnreadUserIdList(unreadUserIdList);
    	}

    	ChatMessageDao dao = new ChatMessageDao(message.getMessageId(), 
    			message.getType(), 
    			message.getRoomId(), 
    			message.getUserId(), 
    			message.getMessage(), 
    			message.getParentMessageId(),
    			message.getCreatedTime());
    	

    	if(message.getType()==ChatMessageEnum.MSG_FILE) {
    		dao.setMimeType(message.getMimeType());
    		dao.setDownloadPath(message.getDownloadPath());
    	}
    	if(message.getType()==ChatMessageEnum.MSG_NOTI) {
    		dao.setNType(message.getNType());
    	}
    	mChatMessageRepository.save(dao);
    	
    	if(message.getType()==ChatMessageEnum.MSG_FILE) {
    		message.setDownloadPath(mDownloadUrl + message.getDownloadPath());
    	}
    	mRedisTemplate.convertAndSend(mChannelTopic.getTopic(), message);
    }
    
    public ResponseEntity<?> upload(MultipartFile file, String roomId, String userId, HttpServletRequest request) {
    	
        File srcFile = new File(file.getOriginalFilename());
        Path srcPath = srcFile.toPath();
        String fileExt = FilenameUtils.getExtension(file.getOriginalFilename());
        String mimeType = "";
        try {
			mimeType = Files.probeContentType(srcPath);
			
		} catch (IOException e) {
        	return  ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_ERROR_GET_FILE_CONTENT_TYPE, 
        												ErrorCodeEnum.CODE_ERROR_GET_FILE_CONTENT_TYPE.toString()));
		}
    	
    	String dstFilename = UUID.randomUUID().toString() + "." + fileExt;
    	File uploadFile = null;
    	try {
    		uploadFile = convert(file, dstFilename);
    	} catch(FileStorageException e) {
        	return  ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_ERROR_COPY_FILE_TO_STORAGE, 
														ErrorCodeEnum.CODE_ERROR_COPY_FILE_TO_STORAGE.toString()));
    	}
    	//AWS
    	/*
        String fileDownloadUri = request.getHeader("X-FORWARDED-HOST");
        fileDownloadUri = fileDownloadUri.replace(":80", "");
        fileDownloadUri = "https://" + fileDownloadUri;
        fileDownloadUri = fileDownloadUri + "/chat/download/";
        fileDownloadUri = fileDownloadUri + uploadFile.getName();
        */
    	
    	//Web Test
    	/*
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/chat/download/")
                .path(uploadFile.getName())
                .toUriString();
        */
    	
    	//Mobile Test
    	/*
        String fileDownloadUri = "http://127.0.0.1:3030/chat/download/";//"http://112.220.119.251:3030/chat/download/";
        fileDownloadUri = fileDownloadUri + uploadFile.getName();
        */
    	
        ChatMessage message =  ChatMessage.builder()
					        	.messageId(UUID.randomUUID().toString())
					        	.messageType(ChatMessageEnum.MSG_FILE)
					        	.roomId(roomId)
					        	.userId(userId)
					        	.mimeType(mimeType)
					        	.downloadPath(uploadFile.getName())
					        	.createdTime(TimeUtil.unixTime())
					        	.build();
        
        send(message);
        
        String fileDownloadUri = mDownloadUrl + uploadFile.getName();
        
    	return ResponseEntity.ok(UploadResponse.builder().fileName(uploadFile.getName())
    													.fileDownloadUrl(fileDownloadUri)
    													.fileSize(file.getSize())
    													.fileType(mimeType)
    													.build());
    }
    
    public ResponseEntity<?> uploadComment(MultipartFile file, String roomId, String userId, String parentMessageId, HttpServletRequest request) {
    	
        File srcFile = new File(file.getOriginalFilename());
        Path srcPath = srcFile.toPath();
        String fileExt = FilenameUtils.getExtension(file.getOriginalFilename());
        String mimeType = "";
        try {
			mimeType = Files.probeContentType(srcPath);
			
		} catch (IOException e) {
        	return  ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_ERROR_GET_FILE_CONTENT_TYPE, 
        												ErrorCodeEnum.CODE_ERROR_GET_FILE_CONTENT_TYPE.toString()));
		}
    	
    	String dstFilename = UUID.randomUUID().toString() + "." + fileExt;
    	File uploadFile = null;
    	try {
    		uploadFile = convert(file, dstFilename);
    	} catch(FileStorageException e) {
        	return  ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_ERROR_COPY_FILE_TO_STORAGE, 
														ErrorCodeEnum.CODE_ERROR_COPY_FILE_TO_STORAGE.toString()));
    	}
    	//AWS
    	/*
        String fileDownloadUri = request.getHeader("X-FORWARDED-HOST");
        fileDownloadUri = fileDownloadUri.replace(":80", "");
        fileDownloadUri = "https://" + fileDownloadUri;
        fileDownloadUri = fileDownloadUri + "/chat/download/";
        fileDownloadUri = fileDownloadUri + uploadFile.getName();
        */
    	
    	//Web Test
    	/*
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/chat/download/")
                .path(uploadFile.getName())
                .toUriString();
        */
    	
    	//Mobile Test
    	/*
        String fileDownloadUri = "http://127.0.0.1:3030/chat/download/";//"http://112.220.119.251:3030/chat/download/";
        fileDownloadUri = fileDownloadUri + uploadFile.getName();
        */
        
    	
        ChatMessage message =  ChatMessage.builder()
					        	.messageId(UUID.randomUUID().toString())
					        	.messageType(ChatMessageEnum.MSG_FILE)
					        	.roomId(roomId)
					        	.userId(userId)
					        	.mimeType(mimeType)
					        	.downloadPath(uploadFile.getName())
					        	.createdTime(TimeUtil.unixTime())
					        	.build();
        message.setParentMessageId(parentMessageId);
        
        send(message);
        
        String fileDownloadUri = mDownloadUrl + uploadFile.getName();
    	return ResponseEntity.ok(UploadResponse.builder().fileName(uploadFile.getName())
    													.fileDownloadUrl(fileDownloadUri)
    													.fileSize(file.getSize())
    													.fileType(mimeType)
    													.build());
    }
    
    public ResponseEntity<?> download(String fileName, HttpServletRequest request) {
        try {
        	Path filePath = Paths.get(mFileStorageLocation, fileName).normalize();
        	org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                String contentType = null;
                try {
                    contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
                } catch (IOException ex) {
                	return  ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_ERROR_GET_FILE_CONTENT_TYPE, 
																ErrorCodeEnum.CODE_ERROR_GET_FILE_CONTENT_TYPE.toString()));
                }
                
                if(contentType == null) {
                    contentType = "application/octet-stream";
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
            	return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_ERROR_COPY_FILE_TO_STORAGE, 
															ErrorCodeEnum.CODE_ERROR_COPY_FILE_TO_STORAGE.toString()));
            }
        } catch (MalformedURLException ex) {
        	return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_ERROR_COPY_FILE_TO_STORAGE, 
														ErrorCodeEnum.CODE_ERROR_COPY_FILE_TO_STORAGE.toString()));
        }
    }
    
    public int getUnreadMessageCnt(String roomId, String userId) {
    	return (mChatMessageStatusRepository.countByRoomIdAndUserId(roomId, userId)).intValue();
    }

    private File convert(MultipartFile srcMPFile, String dstFilename) throws FileStorageException {
        String srcFileName = StringUtils.cleanPath(srcMPFile.getOriginalFilename());
        if(srcFileName.contains("..")) {
            throw new FileStorageException("Sorry! Filename contains invalid path sequence " + srcFileName);
        }
       
        try {
	        File dstFile = new File(mFileStorageLocation + "/" + dstFilename);
	        if (dstFile.createNewFile()) {
	            try (FileOutputStream fos = new FileOutputStream(dstFile)) {
	                fos.write(srcMPFile.getBytes());
	            }
	            return dstFile;
	        }   
        } catch(IOException e) {
        	throw new FileStorageException(e.getMessage());
        }   
        return null;
    }
    
    /*
    private static final String CHAT_MESSAGE_BY_TIME = "CMBT_"; // 시간순서별 메시지 
    private static final String CHAT_MESSAGE_BY_RELATIONSHIP = "CMBR_"; // 연관관계 
    @Value("${spring.message.count}")
    private int mMessageCount;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> mRoomMessage;
    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> mRoomMessageSeq;
    */
    
    /*
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
							.mimeType(converted.getMimeType())
							.downloadPath(converted.getDownloadPath())
							.createdTime(converted.getCreatedTime())
							.build();
					
					if(convertedParent!=null) {
						ChatMessage parentMessage = ChatMessage.builder()
														.messageId(convertedParent.getMessageId())
														.messageType(convertedParent.getType())
														.roomId(convertedParent.getRoomId())
														.userId(convertedParent.getUserId())
														.message(convertedParent.getMessage())
														.mimeType(convertedParent.getMimeType())
														.downloadPath(convertedParent.getDownloadPath())
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
    */
    
    /*
    public void sendChatMessage(ChatMessage message) {
    	RedisChatMessage redisMessage = new RedisChatMessage();
    	redisMessage.setMessageId(message.getMessageId());
    	redisMessage.setType(message.getType());
    	redisMessage.setRoomId(message.getRoomId());
    	redisMessage.setUserId(message.getUserId());
    	redisMessage.setMessage(message.getMessage());
    	redisMessage.setMimeType(message.getMimeType());
    	redisMessage.setDownloadPath(message.getDownloadPath());
    	redisMessage.setCreatedTime(message.getCreatedTime());
    	
    	ObjectMapper objMapper = new ObjectMapper();
    	try {
    		String jsonRedisMessage  = objMapper.writeValueAsString(redisMessage);
    		mRoomMessage.put(CHAT_MESSAGE_BY_TIME + message.getRoomId(), redisMessage.getMessageId(), jsonRedisMessage);
    		//mRoomMessage.get(CHAT_MESSAGE_BY_TIME + message.getRoomId(), redisMessage.getMessageId());
    		//mRoomMessage.delete(CHAT_MESSAGE_BY_TIME + message.getRoomId(), redisMessage.getMessageId());
    		
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
															.mimeType(convertedParent.getMimeType())
															.downloadPath(convertedParent.getDownloadPath())
															.createdTime(convertedParent.getCreatedTime())
															.build();
							message.setParentMessage(parentMessage);
						}
					}
				}
        	}
    		
    		Long size = mRoomMessageSeq.size(message.getRoomId());
    		mRoomMessageSeq.add(message.getRoomId(), redisMessage.getMessageId(), size + 1);

            mRedisTemplate.convertAndSend(mChannelTopic.getTopic(), message);
		} catch (JsonProcessingException e) {}
    }
    */
}