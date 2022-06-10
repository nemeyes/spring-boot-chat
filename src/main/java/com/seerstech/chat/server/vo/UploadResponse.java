package com.seerstech.chat.server.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UploadResponse extends SuccessResponse {

	@JsonProperty("file_name")
	String fileName;
	
	@JsonProperty("file_download_url")
	String fileDownloadUrl;
	
	@JsonProperty("file_original_name")
	String fileOriginalName;
	
	@JsonProperty("file_type")
	String fileType;
	
	@JsonProperty("file_size")
	long fileSize;
}
