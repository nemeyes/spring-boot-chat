package com.seerstech.chat.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seerstech.chat.server.config.FileStorageConfig;
import com.seerstech.chat.server.exception.FileStorageException;
import com.seerstech.chat.server.model.ChatMessageEnum;
import com.seerstech.chat.server.redis.RedisChatMessage;
import com.seerstech.chat.server.utils.TimeUtil;
import com.seerstech.chat.server.vo.ChatMessage;
import com.seerstech.chat.server.vo.ErrorCodeEnum;
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.UrlResource;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class ChatMessageService {
	
    private static final String CHAT_MESSAGE_BY_TIME = "CMBT_"; // 시간순서별 메시지 
    private static final String CHAT_MESSAGE_BY_RELATIONSHIP = "CMBR_"; // 연관관계
	
    @Value("${spring.message.count}")
    private int mMessageCount;
    
    private final ChannelTopic mChannelTopic;
    private final RedisTemplate<?, ?> mRedisTemplate;
    
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> mRoomMessage;
    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> mRoomMessageSeq;
    
    @Resource(name = "uploadDir")
    private final String mFileStorageLocation;
    
    @Autowired
    public ChatMessageService(ChannelTopic channelTopic, RedisTemplate<?, ?> redisTemplate, String uploadDir) {
		this.mChannelTopic = channelTopic;
		this.mRedisTemplate = redisTemplate;
		this.mFileStorageLocation = uploadDir;    	
    }
    
    public String getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf('/');
        if (lastIndex != -1)
            return destination.substring(lastIndex + 1);
        else
            return "";
    }
    
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
    	
        String fileDownloadUri = request.getHeader("X-FORWARDED-HOST");
        fileDownloadUri = fileDownloadUri.replace(":80", "");
        fileDownloadUri = "https://" + fileDownloadUri;
        fileDownloadUri = fileDownloadUri + "/chat/download/";
        fileDownloadUri = fileDownloadUri + uploadFile.getName();
        /*
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/chat/download/")
                .path(uploadFile.getName())
                .toUriString();
        */
        
        ChatMessage message =  ChatMessage.builder()
					        	.messageId(UUID.randomUUID().toString())
					        	.messageType(ChatMessageEnum.MSG_FILE)
					        	.roomId(roomId)
					        	.userId(userId)
					        	.mimeType(mimeType)
					        	.downloadPath(fileDownloadUri)
					        	.createdTime(TimeUtil.unixTime())
					        	.build();
        
        sendChatMessage(message);
        
    	return ResponseEntity.ok(UploadResponse.builder().fileName(uploadFile.getName())
    													.fileDownloadUrl(fileDownloadUri)
    													.fileSize(file.getSize())
    													.fileType(mimeType)
    													.build());
    }
    
    //@Profile("dev")
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
}