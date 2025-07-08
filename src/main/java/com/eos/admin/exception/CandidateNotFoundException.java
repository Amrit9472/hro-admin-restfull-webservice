package com.eos.admin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CandidateNotFoundException extends RuntimeException {

	public CandidateNotFoundException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
