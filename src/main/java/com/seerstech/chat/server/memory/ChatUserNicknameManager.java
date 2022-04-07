package com.seerstech.chat.server.memory;

import java.util.Hashtable;

/*
public class ChatUserNicknameManager {

	private static ChatUserNicknameManager mInstance;
	
    private Hashtable<String, String> mWebSocketSessionIdUserId = new Hashtable<String, String>();    
    private Hashtable<String, String> mUserIdNicknames = new Hashtable<String, String>();
    
    static {
    	try { 
    		mInstance = new ChatUserNicknameManager();
    	} catch(Exception e) {
    		throw new RuntimeException("Create instace fail. error msg = " + e.getMessage() );
    	}
    }
    
    public static ChatUserNicknameManager getInstance() {
        return mInstance;
    }
    
    public synchronized void putUserNickname(String websocketSessionId, String userId, String userNickname) {
    	mWebSocketSessionIdUserId.put(websocketSessionId, userId);
    	mUserIdNicknames.put(userId, userNickname);
    }
    
    public synchronized String getUserNicknameByWebsocketSessionId(String websocketSessionId) {
    	String nickname = null;
    	if(mWebSocketSessionIdUserId.containsKey(websocketSessionId)) {
    		String userId = mWebSocketSessionIdUserId.get(websocketSessionId);
    		if(mUserIdNicknames.containsKey(userId)) {
    			nickname = mUserIdNicknames.get(userId);
    		}
    	}
    	return nickname;
    }
    
    public synchronized String getUserNicknameByUserId(String userId) {
    	String nickname = null;
		if(mUserIdNicknames.containsKey(userId)) {
			nickname = mUserIdNicknames.get(userId);
		}
    	return nickname;
    }
    
    public synchronized void removeUserNicknameByWebsocketSessionId(String websocketSessionId) {
    	if(mWebSocketSessionIdUserId.containsKey(websocketSessionId)) {
    		String userId = mWebSocketSessionIdUserId.get(websocketSessionId);
    		if(mUserIdNicknames.containsKey(userId)) {
    			mUserIdNicknames.remove(userId);
    		}
    		mWebSocketSessionIdUserId.remove(websocketSessionId);
    	}
    }
}
*/
