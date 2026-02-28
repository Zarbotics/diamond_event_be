package com.zbs.de.util.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.zbs.de.util.ResponseMessage;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ResponseMessage> handleRuntimeException(RuntimeException ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
						"Internal Server Error: " + ex.getMessage()));
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ResponseMessage> handleEntityNotFound(EntityNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessage(HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND, "Resource not found: " + ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseMessage> handleValidation(MethodArgumentNotValidException ex) {
		String error = ex.getBindingResult().getFieldError().getDefaultMessage();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST, "Validation error: " + error));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseMessage> handleAllOtherExceptions(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
						"Unexpected error occurred: " + ex.getMessage()));
	}
	
	
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ResponseMessage> handleMaxSizeException(MaxUploadSizeExceededException ex) {

		return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new ResponseMessage(
				HttpStatus.PAYLOAD_TOO_LARGE.value(), HttpStatus.PAYLOAD_TOO_LARGE, "File must not exceed 3MB"));

	}
}