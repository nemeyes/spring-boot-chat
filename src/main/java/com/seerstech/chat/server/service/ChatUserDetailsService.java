package com.seerstech.chat.server.service;

import com.seerstech.chat.server.constant.ChatRoleEnum;
import com.seerstech.chat.server.model.ChatRoleDao;
import com.seerstech.chat.server.model.ChatUserDao;
import com.seerstech.chat.server.repo.ChatRoleRepository;
import com.seerstech.chat.server.repo.ChatUserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatUserDetailsService implements UserDetailsService {
	
	@Autowired
	private ChatUserRepository mChatUserRepository;
	
	@Autowired
	private ChatRoleRepository mChatRoleRepository;
	
	
	public ChatUserDao findUserByUserId(String userId) {
		return mChatUserRepository.findByUserId(userId);
	}
	
	public boolean existUserByUserId(String userId) {
		return mChatUserRepository.existsByUserId(userId);
	}
	
	public void saveChatUser(ChatUserDao user) {
		user.setEnabled(true);
		mChatUserRepository.save(user);
	}
	
	public boolean existRoleByRole(ChatRoleEnum role) {
		return mChatRoleRepository.existsByRole(role);
	}
	
	public void saveChatRole(ChatRoleDao role) {
		mChatRoleRepository.save(role);
	}
	
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		ChatUserDao user = mChatUserRepository.findByUserId(username);
		if(user!=null) {
			return ChatUserDetails.build(user);
		} else {
			throw new UsernameNotFoundException("User Not Found with ID : " + username);
		}
	}
	
	public boolean existsByUserId(String userId) {
		return mChatUserRepository.existsByUserId(userId);
	}
}
