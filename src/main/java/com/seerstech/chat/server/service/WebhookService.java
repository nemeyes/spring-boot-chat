package com.seerstech.chat.server.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.common.net.HttpHeaders;
import com.seerstech.chat.server.model.WebhookDao;
import com.seerstech.chat.server.repo.WebhookRepository;
import com.seerstech.chat.server.vo.ChatMessage;
import com.seerstech.chat.server.vo.GetWebhookInfoResponse;
import com.seerstech.chat.server.vo.RegistWebhookResponse;
import com.seerstech.chat.server.vo.UnregistWebhookResponse;
import com.seerstech.chat.server.webhook.ChatUnreadNotificationRequest;
import com.seerstech.chat.server.webhook.ChatUnreadNotificationResponse;

@Service
public class WebhookService {
	
	@Autowired
	WebhookRepository mWebhookRepo;

	private SimpleAsyncTaskExecutor taskExecutor;
	
	public WebhookService() {
		this.taskExecutor = new SimpleAsyncTaskExecutor();
		this.taskExecutor.setConcurrencyLimit(10);
	}
	
    public RegistWebhookResponse registWebhook(String baseUrl, String path) {
    	try {
    		RegistWebhookResponse response = new RegistWebhookResponse();
    		
    		if(mWebhookRepo.count()>0) {
    			mWebhookRepo.deleteAll();
    		}
    		
    		WebhookDao dao = new WebhookDao();
    		dao.setBaseUrl(baseUrl);
    		dao.setPath(path);
    		mWebhookRepo.save(dao);
        	
    		response.setBaseUrl(baseUrl);
    		response.setPath(path);
			return response;
    		
    	} catch(Exception e) {
    		return null;
    	}
    }
    
    public UnregistWebhookResponse unregistWebhook() {
    	try {
    		
    		mWebhookRepo.deleteAll();
    		
    		UnregistWebhookResponse response = new UnregistWebhookResponse();
			return response;
    		
    	} catch(Exception e) {
    		return null;
    	}
    }
    
    public GetWebhookInfoResponse info() {
    	try {
    		
    		List<WebhookDao> webhook = mWebhookRepo.findAll();
    		if(webhook.size()>0) {
    			WebhookDao whs = webhook.get(0);
    			
        		GetWebhookInfoResponse response = new GetWebhookInfoResponse();
        		response.setBaseUrl(whs.getBaseUrl());
        		response.setPath(whs.getPath());
    			return response;
    			
    		} else {
    			return null;
    		}
    	} catch(Exception e) {
    		return null;
    	}
    }

    public void sendChatUnreadNotification(ChatMessage message, List<String> userList) {
    	Runnable runnable = () -> {
    		
    		/*
    		System.out.println(message.getType().toString());
    		System.out.println(message.getMessage());
    		userList.forEach(s -> {
    			System.out.println(s);
    		});
    		*/
    		
    		List<WebhookDao> webhook = mWebhookRepo.findAll();
    		if(webhook.size()>0) {
    			WebhookDao whs = webhook.get(0);
    			
    			
    			try {
	        		WebClient client = WebClient.builder()
							.baseUrl(whs.getBaseUrl())
							.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
	
					ChatUnreadNotificationRequest noti = new ChatUnreadNotificationRequest();
					noti.setMessage(message);
					noti.setUsers(userList);
					ChatUnreadNotificationResponse response = client.post()
																.uri(whs.getPath())
																.bodyValue(noti)
																.retrieve()
																.bodyToMono(ChatUnreadNotificationResponse.class)
																.block();
    			} catch(Exception e) {
    				
    			}
    		}
    		

    		
    	};
    	taskExecutor.execute(runnable);
    }
}
