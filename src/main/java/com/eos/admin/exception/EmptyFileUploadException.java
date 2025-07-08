package com.eos.admin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmptyFileUploadException extends RuntimeException {

	public EmptyFileUploadException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
