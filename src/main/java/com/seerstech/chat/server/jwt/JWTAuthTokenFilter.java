package com.seerstech.chat.server.jwt;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class JWTAuthTokenFilter extends OncePerRequestFilter {

	private static final String CHAT_USER_SESSION_INFOS = "CUSI_";
	//private static final String CHAT_USER_SESSION_BLACKLIST = "CUSB_";
	
	@Autowired
	private JWTTokenProvider mJWTTokenProvider;
	
    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> mJWTTokenSessionInfos;
    
	//@Autowired
	//private ChatUserDetailsService mChatUserDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		
		try {
	      String accessToken = JWTTokenParser.parse(request);
	      if (accessToken != null && mJWTTokenProvider.validateToken(accessToken)) {
	    	  
	    	  Authentication auth = mJWTTokenProvider.getAuthentication(accessToken);
	    	  
	    	  String isLogin = mJWTTokenSessionInfos.get(CHAT_USER_SESSION_INFOS + auth.getName());
	    	  if(!ObjectUtils.isEmpty(isLogin)) {
	    		  SecurityContextHolder.getContext().setAuthentication(auth);
	    	  }
	    	}
	      
	    } catch (Exception e) {
	      logger.error("Cannot set user authentication: {}", e);
	    }

		filterChain.doFilter(request, response);
	}
}
