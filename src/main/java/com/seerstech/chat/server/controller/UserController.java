package com.seerstech.chat.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import lombok.RequiredArgsConstructor;

import com.seerstech.chat.server.vo.SignupRequest;
import com.seerstech.chat.server.vo.SignupResponse;
import com.seerstech.chat.server.vo.SuccessResponse;
import com.seerstech.chat.server.vo.UpdateUserRequest;
import com.seerstech.chat.server.vo.UpdateUserResponse;
import com.seerstech.chat.server.vo.ChatUserResponse;
import com.seerstech.chat.server.vo.ErrorResponse;
import com.seerstech.chat.server.vo.LoginResponse;
import com.seerstech.chat.server.vo.LogoutRequest;
import com.seerstech.chat.server.vo.LogoutResponse;
import com.seerstech.chat.server.vo.ReissueRequest;
import com.seerstech.chat.server.vo.ReissueResponse;
import com.seerstech.chat.server.vo.LoginRequest;
import com.seerstech.chat.server.constant.ChatRoleEnum;
import com.seerstech.chat.server.constant.ErrorCodeEnum;
import com.seerstech.chat.server.jwt.JWTTokenInfo;
import com.seerstech.chat.server.jwt.JWTTokenParser;
import com.seerstech.chat.server.jwt.JWTTokenProvider;
import com.seerstech.chat.server.repo.ChatRoleRepository;
import com.seerstech.chat.server.model.ChatRoleDao;
import com.seerstech.chat.server.model.ChatUserDao;
import com.seerstech.chat.server.service.ChatUserDetails;
import com.seerstech.chat.server.service.ChatUserDetailsService;
import com.seerstech.chat.server.utils.TimeUtil;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

	private static final String CHAT_USER_SESSION_INFOS = "CUSI_";
	//private static final String CHAT_USER_SESSION_BLACKLIST = "CUSB_";
	
	@Autowired
    private JWTTokenProvider mJWTTokenProvider;
	
	@Autowired
	private AuthenticationManager mAuthManager;
    
	@Autowired
	private ChatUserDetailsService mChatUserService;
	
	@Autowired
	private ChatRoleRepository mChatRoleRepository;
	
	@Autowired
	PasswordEncoder mPasswordEncoder;
	
    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> mUserSessionInfos;
    
    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
    	
    	if(mChatUserService.existsByUserId(req.getUserId())) {
			return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_USER_ID_EXIST, ErrorCodeEnum.CODE_USER_ID_EXIST.toString()));
    	}
    	
		ChatUserDao user = new ChatUserDao();
		user.setUserId(req.getUserId());
		user.setUserPassword(mPasswordEncoder.encode(req.getUserPassword()));
		user.setUserNickname(req.getUserNickname());
		user.setCreatedTime(TimeUtil.unixTime());
		
		Set<String> strRoles = req.getUserRoles();
		Set<ChatRoleDao> roles = new HashSet<>();
		
		if(strRoles==null) {
			ChatRoleDao userRole = mChatRoleRepository.findByRole(ChatRoleEnum.ROLE_USER);
			if(userRole==null) {
				new RuntimeException("Error: Role is not found.");
			}
			roles.add(userRole);
		} else {
			strRoles.forEach(role->{
				switch (role) {
				case "admin":
					ChatRoleDao adminRole = mChatRoleRepository.findByRole(ChatRoleEnum.ROLE_ADMIN);
					if(adminRole==null) {
						new RuntimeException("Error: Role is not found.");
					}
					roles.add(adminRole);
					break;
				default:
					ChatRoleDao userRole = mChatRoleRepository.findByRole(ChatRoleEnum.ROLE_USER);
					if(userRole==null) {
						new RuntimeException("Error: Role is not found.");
					}
					roles.add(userRole);
				}
			});
		}
		user.setUserRoles(roles); 
		mChatUserService.saveChatUser(user);
		return ResponseEntity.ok(new SignupResponse());
    }
    
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    	ChatUserDao userDoc = mChatUserService.findUserByUserId(req.getUserId());

		if(userDoc==null) {
			return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_USER_NOT_FOUND, ErrorCodeEnum.CODE_USER_NOT_FOUND.toString()));
		} else {
	    	Authentication auth = mAuthManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUserId(), req.getUserPassword()));
	    	SecurityContextHolder.getContext().setAuthentication(auth);
	    	JWTTokenInfo jwt = mJWTTokenProvider.generateToken(auth);
	    	
	    	ChatUserDetails user = (ChatUserDetails) auth.getPrincipal();
	    	List<String> roles = user.getAuthorities().stream().map(item->item.getAuthority()).collect(Collectors.toList());
	    	Iterator<String> iter = roles.iterator();
	    	boolean bAdmin = false;
	    	while(iter.hasNext()) {
	    		String role = iter.next();
	    		if(role.equals(ChatRoleEnum.ROLE_ADMIN.toString())) {
	    			bAdmin = true;
	    			break;
	    		}
	    	}
	    	
	    	mUserSessionInfos.set(CHAT_USER_SESSION_INFOS + auth.getName(), jwt.getRefreshToken(), jwt.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);
	    	
			return ResponseEntity.ok(LoginResponse.builder()
										.grantType(jwt.getGrantType())
										.accessToken(jwt.getAccessToken())
										.refreshToken(jwt.getRefreshToken())
										.refreshTokenExpirationTime(jwt.getRefreshTokenExpirationTime())
										.userId(user.getUserId())
										.userNickname(user.getUserNickname())
										.userRole(bAdmin?"admin":"user")
										.build());
		}
    }
    
    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<?> logout(@RequestBody LogoutRequest req) {
    	if(!mJWTTokenProvider.validateToken(req.getAccessToken())) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_INVALID_REQUEST, ErrorCodeEnum.CODE_INVALID_REQUEST.toString()));
    	}
    	
    	Authentication auth = mJWTTokenProvider.getAuthentication(req.getAccessToken());
    	if(mUserSessionInfos.get(CHAT_USER_SESSION_INFOS + auth.getName()) != null) {
    		mUserSessionInfos.getAndDelete(CHAT_USER_SESSION_INFOS + auth.getName());
    	}
    	
    	//Long expiration = mJWTTokenProvider.getExpiration(req.getAccessToken());
    	//mUserSessionInfos.set(CHAT_USER_SESSION_BLACKLIST + req.getAccessToken(), "logout", expiration, TimeUnit.MILLISECONDS);
    	return ResponseEntity.ok(LogoutResponse.builder().build());
    }
    
    @PostMapping("/reissue")
    @ResponseBody
    public ResponseEntity<?> reissue(@RequestBody ReissueRequest req) {
    	
    	if(!mJWTTokenProvider.validateToken(req.getRefreshToken())) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_INVALID_REFRESH_TOKEN, ErrorCodeEnum.CODE_INVALID_REFRESH_TOKEN.toString()));
    	}
    	
    	Authentication auth = mJWTTokenProvider.getAuthentication(req.getAccessToken());
    	
    	String refreshToken = mUserSessionInfos.get(CHAT_USER_SESSION_INFOS + auth.getName());
    	if(ObjectUtils.isEmpty(refreshToken)) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_INVALID_REQUEST, ErrorCodeEnum.CODE_INVALID_REQUEST.toString()));
    	}
    	
    	if(!refreshToken.equals(req.getRefreshToken())) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_REFRESH_TOKEN_NOT_MATCH, ErrorCodeEnum.CODE_REFRESH_TOKEN_NOT_MATCH.toString()));
    	}
    	
    	JWTTokenInfo jwt = mJWTTokenProvider.generateToken(auth);
    	mUserSessionInfos.set(CHAT_USER_SESSION_INFOS + auth.getName(), jwt.getRefreshToken(), jwt.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);
    
		return ResponseEntity.ok(ReissueResponse.builder()
				.grantType(jwt.getGrantType())
				.accessToken(jwt.getAccessToken())
				.refreshToken(jwt.getRefreshToken())
				.refreshTokenExpirationTime(jwt.getRefreshTokenExpirationTime())
				.build());
    }
    
    
    @GetMapping("/{userId}")
    @ResponseBody
    public ResponseEntity<?> user(@RequestHeader (name="Authorization") String token, @PathVariable String userId) {
    	String jwtToken = JWTTokenParser.parse(token);
    	String selfId = mJWTTokenProvider.getUserNameFromJwt(jwtToken);
    	if(selfId.equals(userId)) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_CANNOT_BE_ALLOWED_TO_FIND_YOURSELF, ErrorCodeEnum.CODE_CANNOT_BE_ALLOWED_TO_FIND_YOURSELF.toString()));
    	} else {
    		ChatUserDao user = mChatUserService.findUserByUserId(userId);
    		if(user==null) {
    			return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_USER_NOT_FOUND, ErrorCodeEnum.CODE_USER_NOT_FOUND.toString()));
    		} else {
    			return ResponseEntity.ok(new ChatUserResponse(user.getUserId(), user.getUserNickname()));
    		}
    	}
    }
    
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> update(@RequestHeader (name="Authorization") String token, @RequestBody UpdateUserRequest req) {
    	
    	if(req.getUserNickname()==null || req.getUserNickname().isEmpty()) {
    		return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_INVALID_REQUEST, ErrorCodeEnum.CODE_INVALID_REQUEST.toString()));
    	}
    	
    	String jwtToken = JWTTokenParser.parse(token);
    	String userId = mJWTTokenProvider.getUserNameFromJwt(jwtToken);
    	
		ChatUserDao user = mChatUserService.findUserByUserId(userId);
		if(user==null) {
			return ResponseEntity.ok(new ErrorResponse(ErrorCodeEnum.CODE_USER_NOT_FOUND, ErrorCodeEnum.CODE_USER_NOT_FOUND.toString()));
		} else {
			user.setUserNickname(req.getUserNickname());
			mChatUserService.saveChatUser(user);
			
	    	UpdateUserResponse response = new UpdateUserResponse(user.getUserId(), user.getUserNickname());
	    	return ResponseEntity.ok(response);	
		}
    }
}
