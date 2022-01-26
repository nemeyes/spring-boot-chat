package com.seerstech.chat.server.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.seerstech.chat.server.model.ChatRoleDao;
import com.seerstech.chat.server.model.ChatRoleEnum;
import com.seerstech.chat.server.model.ChatUserDao;
import com.seerstech.chat.server.repo.ChatRoleRepository;
import com.seerstech.chat.server.utils.TimeUtil;

@Component
public class ChatServiceRunner implements CommandLineRunner {

	@Autowired
	PasswordEncoder mPasswordEncoder;
	
	@Autowired
	ChatUserDetailsService mChatUserService;
	
	@Autowired
	private ChatRoleRepository mChatRoleRepository;
	
    @Override
    public void run(String... args) throws Exception {
    	
    	boolean bRoleAdmin = mChatUserService.existRoleByRole(ChatRoleEnum.ROLE_ADMIN);
    	if(!bRoleAdmin) {
    		ChatRoleDao role = new ChatRoleDao();
    		role.setRole(ChatRoleEnum.ROLE_ADMIN);
    		mChatUserService.saveChatRole(role);
    	}
    	
    	boolean bRoleUser = mChatUserService.existRoleByRole(ChatRoleEnum.ROLE_USER);
    	if(!bRoleUser) {
    		ChatRoleDao role = new ChatRoleDao();
    		role.setRole(ChatRoleEnum.ROLE_USER);
    		mChatUserService.saveChatRole(role);
    	}

    	boolean exist = mChatUserService.existsByUserId("admin");
    	if(!exist) {
    		ChatUserDao user = new ChatUserDao();
    		user.setUserId("admin");
    		user.setUserPassword(mPasswordEncoder.encode("seers.chat"));
    		user.setUserNickname("admin");
    		user.setCreatedTime(TimeUtil.unixTime());
    		
    		Set<ChatRoleDao> roles = new HashSet<>();
    		ChatRoleDao adminRole = mChatRoleRepository.findByRole(ChatRoleEnum.ROLE_ADMIN);
    		roles.add(adminRole);
    		user.setUserRoles(roles);
    		
    		mChatUserService.saveChatUser(user);
    	}
    }
}