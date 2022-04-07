package com.seerstech.chat.server.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.seerstech.chat.server.service.ChatUserDetails;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Resource;

@Slf4j
@Component
public class JWTTokenProvider {

    @Value("${spring.jwt.secret}")
    private String mJWTSecretKey;
    
    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";
    //private static final long ACCESS_TOKEN_EXPIRE_TIME = 30 * 60 * 1000L; //30분만 토큰 유효
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 10 * 24 * 60 * 60 * 1000L; //10일동안 토큰 유효 
    
    @Resource(name = "accessTokenExpTime")
    private final long mAccessTokenExpTime;
    
    @Autowired
    public JWTTokenProvider(long mAccessTokenExpTime) {
		this.mAccessTokenExpTime = mAccessTokenExpTime;
    }
    
    public JWTTokenInfo generateToken(Authentication auth) {
    	
    	String authorities = auth.getAuthorities().stream()
                					.map(GrantedAuthority::getAuthority)
                					.collect(Collectors.joining(","));

    	Date now = new Date();
    	//long now = (new Date()).getTime();
    	Date accessTokenExpiresIn = new Date(now.getTime() + mAccessTokenExpTime);
    	String accessToken =  Jwts.builder()
    							.setSubject(auth.getName())
    							//.setIssuedAt(now)
    							.claim(AUTHORITIES_KEY, authorities)
    							.setExpiration(accessTokenExpiresIn)
    							.signWith(SignatureAlgorithm.HS256, mJWTSecretKey)
    							.compact();
    	
    	String refreshToken = Jwts.builder()
    							.setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME))
    							.signWith(SignatureAlgorithm.HS256, mJWTSecretKey)
    							.compact();
    	
    	return JWTTokenInfo.builder()
    					.grantType(BEARER_TYPE)
    					.accessToken(accessToken)
    					.refreshToken(refreshToken)
    					.refreshTokenExpirationTime(REFRESH_TOKEN_EXPIRE_TIME)
    					.build();
    }

    public String getUserNameFromJwt(String jwt) {
        return parseClaims(jwt).getSubject();
    }
    
    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new ChatUserDetails(claims.getSubject(), authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
    

    public boolean validateToken(String accessToken) {
    	try {
    		Jwts.parser().setSigningKey(mJWTSecretKey).parseClaimsJws(accessToken);
    		return true;
    	} catch (SecurityException ex) {
    		log.info("Invalid JWT Token");
        } catch (SignatureException ex) {
            log.info("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.info("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.info("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.info("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.info("JWT claims string is empty.");
        }
    	return false;
    }
    
    public Long getExpiration(String accessToken) {
        // accessToken 남은 유효시간
        Date expiration = parseClaims(accessToken).getExpiration();
        // 현재 시간
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }
    
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser().setSigningKey(mJWTSecretKey).parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /*
    private Jws<Claims> getClaims(String accessToken) {
        try {
            return Jwts.parser().setSigningKey(mJWTSecretKey).parseClaimsJws(accessToken);
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
            throw ex;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
            throw ex;
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
            throw ex;
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
            throw ex;
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
            throw ex;
        }
    }
    */
}