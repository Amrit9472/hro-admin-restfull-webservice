package com.eos.admin.exception;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.eos.admin.error.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(DuplicateRecordException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateRecordException(DuplicateRecordException ex ,HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				ex.getMessage(),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				request.getRequestURI()
				);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}
	
	@ExceptionHandler(InvalidInputException.class)
	public ResponseEntity<ErrorResponse> handleInvalidInputException(InvalidInputException ex,HttpServletRequest request){
		ErrorResponse error = new ErrorResponse(
				ex.getMessage(),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				request.getRequestURI()
				);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}
	  @ExceptionHandler(ProcessException.class)
	    public ResponseEntity<ErrorResponse> handleProcessException(ProcessException ex ,HttpServletRequest request) {
		  ErrorResponse error = new ErrorResponse(
					ex.getMessage(),
					HttpStatus.BAD_REQUEST.value(),
					HttpStatus.BAD_REQUEST.getReasonPhrase(),
					request.getRequestURI()
					);
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	    }
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex){
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
	}
	@ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex,HttpServletRequest request) {
//        ErrorResponse error = new ErrorResponse(ex.getMessage());
		 ErrorResponse error = new ErrorResponse(
					ex.getMessage(),
					HttpStatus.BAD_REQUEST.value(),
					HttpStatus.BAD_REQUEST.getReasonPhrase(),
					request.getRequestURI()
					);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
	

	@ExceptionHandler(FileAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleFileAlreadyExistException(FileAlreadyExistsException ex , HttpServletRequest request){
		 ErrorResponse error = new ErrorResponse(
					ex.getMessage(),
					HttpStatus.BAD_REQUEST.value(),
					HttpStatus.BAD_REQUEST.getReasonPhrase(),
					request.getRequestURI()
					);
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);	
	}
	@ExceptionHandler(EmptyFileUploadException.class)
	public ResponseEntity<ErrorResponse> handleEmptyFileUploadException(EmptyFileUploadException ex , HttpServletRequest request){
		 ErrorResponse error = new ErrorResponse(
					ex.getMessage(),
					HttpStatus.BAD_REQUEST.value(),
					HttpStatus.BAD_REQUEST.getReasonPhrase(),
					request.getRequestURI()
					);
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);	
	}
	
	@ExceptionHandler(CandidateNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleCandidateNotFoundException(CandidateNotFoundException ex , HttpServletRequest request){
		 ErrorResponse error = new ErrorResponse(
					ex.getMessage(),
					HttpStatus.BAD_REQUEST.value(),
					HttpStatus.BAD_REQUEST.getReasonPhrase(),
					request.getRequestURI()
					);
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);	
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,HttpServletRequest request) {

        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                err -> err.getField(),
                err -> err.getDefaultMessage(),
                (existing, replacement) -> existing
            ));

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Validation failed");
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setPath(request.getRequestURI());
        errorResponse.setFieldErrors(fieldErrors);

        return ResponseEntity.badRequest().body(errorResponse);
    }
}
