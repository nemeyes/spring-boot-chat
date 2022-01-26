package com.seerstech.chat.server.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.seerstech.chat.server.model.ChatRoleDao;
import com.seerstech.chat.server.model.ChatUserDao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatUserDetails implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5692447217517867849L;

	private String id;

	private String userId;

	private String userNickname;

	private String userPassword;

	private Collection<? extends GrantedAuthority> userAuthorities;

	public static ChatUserDetails build(ChatUserDao user) {

		List<GrantedAuthority> authorities = new ArrayList<>();
		Set<ChatRoleDao> roles = user.getUserRoles();
		roles.forEach(role->{
			authorities.add(new SimpleGrantedAuthority(role.getRole().toString()));
		});
		
		
		return new ChatUserDetails(
				user.getId(), 
				user.getUserId(),
				user.getUserNickname(),
				user.getUserPassword(), 
				authorities);
	}
	
	public ChatUserDetails(String userId, Collection<? extends GrantedAuthority> authorities) {
		this.userId = userId;
		this.userAuthorities = authorities;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return userAuthorities;
	}

	@Override
	public String getPassword() {
		return userPassword;
	}

	@Override
	public String getUsername() {
		return userId;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
