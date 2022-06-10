package com.seerstech.chat.server.constant;

public enum ErrorCodeEnum {
	CODE_SUCCESS {
		@Override
		public String toString() {
			return "처리에 성공하였습니다.";
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
	CODE_READ_MARK_NOT_EXIST {
		@Override
		public String toString() {
			return "이미 읽음으로 처리된 메시지 입니다.";
		}		
	},	
	CODE_DELETE_MARK_NOT_ALLOWED {
		@Override
		public String toString() {
			return "본인이 작성한 메시지만 삭제가 가능합니다.";
		}		
	},
	CODE_DELETE_ROOM_IS_ALLOWED_ADMIN_ONLY {
		@Override
		public String toString() {
			return "방을 삭제하기 위해서는 관리자 권한이 필요합니다.";
		}		
	},	
	CODE_NOTIFY_MESSAGE_TO_ROOM_IS_ALLOWED_ADMIN_ONLY {
		@Override
		public String toString() {
			return "방에 알림을 전달하기 위해서는 관리자 권한이 필요합니다.";
		}		
	},	
	CODE_GET_NOTIFY_MESSAGE_FROM_ROOM_IS_ALLOWED_ADMIN_ONLY {
		@Override
		public String toString() {
			return "방 알림을 조회하는 기능은 관리자 권한이 있는 사용자만 가능합니다.";
		}		
	},
	CODE_DELETE_SECION_MESSAGE_FROM_ROOM_IS_ALLOWED_ADMIN_ONLY {
		@Override
		public String toString() {
			return "방의 섹션구간내의 삭제기능은 관리자 권한이 있는 사용자만 가능합니다.";
		}		
	},	
	CODE_DELETE_SECTION_REQUIRE_START_SECTION_MESSAGE {
		@Override
		public String toString() {
			return "섹션구간 삭제를 위한 파라미터로 시작 섹션 메시지가 필요합니다.";
		}		
	},
	CODE_DELETE_SECTION_HAS_END_SECTION_ALONG_WITH_START_SECTION {
		@Override
		public String toString() {
			return "섹션구간 삭제를 시작 섹션과 대응되는 종료 섹션이 필요합니다.";
		}		
	},
	CODE_NO_ROOM_EXIST {
		@Override
		public String toString() {
			return "방(Room) 리스트 정보가 없습니다.";
		}		
	},
	CODE_PAGE_NUMBER_MUST_BE_GREATER_THAN_ZERO {
		@Override
		public String toString() {
			return "페이지 넘버는 0보다 커야합니다.";
		}		
	},
	CODE_PAGE_SIZE_MUST_BE_GREATER_THAN_ZERO {
		@Override
		public String toString() {
			return "페이지 사이즈는 0보다 커야합니다.";
		}		
	},
	CODE_REGIST_WEBHOOK_ERROR {
		@Override
		public String toString() {
			return "웹훅 주소를 등록할 수 없습니다.";
		}		
	},
	CODE_UNREGIST_WEBHOOK_ERROR {
		@Override
		public String toString() {
			return "웹훅 주소를 삭제할 수 없습니다.";
		}		
	},
	CODE_NO_WEBHOOK_INFO {
		@Override
		public String toString() {
			return "웹훅 주소가 존재하지 않습니다.";
		}		
	},
	CODE_WEBHOOK_API_IS_ALLOWED_ADMIN_ONLY {
		@Override
		public String toString() {
			return "웹훅 API는 관리자만이 사용할수 있습니다.";
		}		
	},
}
