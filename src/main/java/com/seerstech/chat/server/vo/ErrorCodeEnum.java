package com.seerstech.chat.server.vo;

public enum ErrorCodeEnum {
	CODE_SUCCESS {
		@Override
		public String toString() {
			return "성공";
		}
	},
	CODE_GENERIC_FAIL {
		@Override
		public String toString() {
			return "알수없는 오류가 발생했습니다.";
		}
	},
	CODE_INVALID_REQUEST {
		@Override
		public String toString() {
			return "유효하지 않은 요청입니다.";
		}
	},
	CODE_CANNOT_BE_ALLOWED_TO_FIND_YOURSELF {
		@Override
		public String toString() {
			return "사용자가 그 자신을 찾을 수 없습니다.";
		}
	},
	CODE_USER_NOT_FOUND {
		@Override
		public String toString() {
			return "사용자를 찾을수 없습니다.";
		}
	},
	CODE_USER_ID_EXIST {
		@Override
		public String toString() {
			return "이미 존재하는 사용자입니다.";
		}		
	},
	CODE_ROOM_NOT_EXIST {
		@Override
		public String toString() {
			return "방(Room)이 존재하지 않습니다.";
		}		
	},
	CODE_NO_USER_EXIST_IN_ROOM {
		@Override
		public String toString() {
			return "해당 방(Room)에는 사용자가 없습니다.";
		}		
	},
	CODE_USER_ALREADY_EXIST_IN_ROOM {
		@Override
		public String toString() {
			return "해당 사용자는 이미 해당 방(Room)에 존재합니다.";
		}		
	},
	CODE_ERROR_INVITE_USER_TO_ROOM {
		@Override
		public String toString() {
			return "사용자 방(Room)에 초대하는 동작중 오류가 발생했습니다.";
		}		
	},
	CODE_ERROR_NOTIFY_TO_ROOM {
		@Override
		public String toString() {
			return "방(Room) 알림기능 동작중 오류가 발생했습니다.";
		}		
	},
	CODE_NO_MESSAGE_IN_ROOM {
		@Override
		public String toString() {
			return "해당 방(Room)에 존재하는 메시자가 없습니다.";
		}		
	},
	CODE_INVALID_REFRESH_TOKEN {
		@Override
		public String toString() {
			return "Refresh Token 정보가 유효하지 않습니다.";
		}		
	},
	CODE_REFRESH_TOKEN_NOT_MATCH {
		@Override
		public String toString() {
			return "Refresh Token 정보가 일치하지 않습니다.";
		}		
	},	
	CODE_ERROR_GET_FILE_CONTENT_TYPE {
		@Override
		public String toString() {
			return "업로드한 파일의 MIME TYPE을 추출과정에서 오류가 발생했습니다.";
		}		
	},	
	CODE_ERROR_COPY_FILE_TO_STORAGE {
		@Override
		public String toString() {
			return "업로드한 파일 저장소에 복사하는 과정에서 오류가 발생했습니다.";
		}		
	},
	CODE_ERROR_FILE_NOT_FOUND {
		@Override
		public String toString() {
			return "파일을 찾을 수 없습니다.";
		}		
	},	
}
