package com.seerstech.chat.server.controller;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import com.seerstech.chat.server.constant.ChatRoleEnum;
import com.seerstech.chat.server.constant.ErrorCodeEnum;
import com.seerstech.chat.server.jwt.JWTTokenParser;
import com.seerstech.chat.server.jwt.JWTTokenProvider;
import com.seerstech.chat.server.model.ChatRoleDao;
import com.seerstech.chat.server.model.ChatUserDao;
import com.seerstech.chat.server.service.ChatUserDetailsService;
import com.seerstech.chat.server.service.WebhookService;
import com.seerstech.chat.server.vo.ErrorResponse;
import com.seerstech.chat.server.vo.GetWebhookInfoResponse;
import com.seerstech.chat.server.vo.RegistWebhookRequest;
import com.seerstech.chat.server.vo.RegistWebhookResponse;
import com.seerstech.chat.server.vo.UnregistWebhookResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class WebhookController {

	private final WebhookService mWebhookService;
	private final JWTTokenProvider mJWTTokenProvider;
    private final ChatUserDetailsService mChatUserService;
    
    @PostMapping("/webhook/regist")
    @ResponseBody
    public ResponseEntity<?> register(@RequestHeader (name="Authorization") String token, @RequestBody RegistWebhookRequest req) {
    	
    	
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
	    	RegistWebhookResponse response = mWebhookService.registWebhook(req.getBaseUrl(), req.getPath());
	    	if(response!=null) {
	    		return ResponseEntity.ok(response);
			} else {
				return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_REGIST_WEBHOOK_ERROR, ErrorCodeEnum.CODE_REGIST_WEBHOOK_ERROR.toString()));
			}
    	} else {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_WEBHOOK_API_IS_ALLOWED_ADMIN_ONLY, ErrorCodeEnum.CODE_WEBHOOK_API_IS_ALLOWED_ADMIN_ONLY.toString()));
    	}
    }
    
    @GetMapping("/webhook/unregist")
    @ResponseBody
    public ResponseEntity<?> unregister(@RequestHeader (name="Authorization") String token) {
    	
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
	    	UnregistWebhookResponse response = mWebhookService.unregistWebhook();
	    	if(response!=null) {
	    		return ResponseEntity.ok(response);
			} else {
				return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_UNREGIST_WEBHOOK_ERROR, ErrorCodeEnum.CODE_UNREGIST_WEBHOOK_ERROR.toString()));
			}
    	} else {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_WEBHOOK_API_IS_ALLOWED_ADMIN_ONLY, ErrorCodeEnum.CODE_WEBHOOK_API_IS_ALLOWED_ADMIN_ONLY.toString()));
    	}
    }
    
    @GetMapping("/webhook/info")
    @ResponseBody
    public ResponseEntity<?> info(@RequestHeader (name="Authorization") String token) {
    	
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
	    	GetWebhookInfoResponse response = mWebhookService.info();
	    	if(response!=null) {
	    		return ResponseEntity.ok(response);
			} else {
				return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_NO_WEBHOOK_INFO, ErrorCodeEnum.CODE_NO_WEBHOOK_INFO.toString()));
			}
    	} else {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_WEBHOOK_API_IS_ALLOWED_ADMIN_ONLY, ErrorCodeEnum.CODE_WEBHOOK_API_IS_ALLOWED_ADMIN_ONLY.toString()));
    	}
    }
}
