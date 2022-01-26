package com.seerstech.chat.server.jwt;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

public class JWTTokenParser {
	
	public static String parse(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");
	    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
	    	return headerAuth.substring(7, headerAuth.length());
	    }
	    return null;
	}  
	
	public static String parse(String token) {
	    if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
	    	return token.substring(7, token.length());
	    }
	    return null;
	}
}
