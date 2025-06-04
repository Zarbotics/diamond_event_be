package com.zbs.de.util;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseMessage {

	private int code;
	private org.springframework.http.HttpStatus status;
	private String message;
	private Object result;

	public ResponseMessage() {
	}

	/**
	 * @param code
	 * @param status
	 * @param message
	 */
	public ResponseMessage(int code, org.springframework.http.HttpStatus status, String message) {
		this.code = code;
		this.status = status;
		this.message = message;
	}

	/**
	 * @param code
	 * @param status
	 * @param message
	 * @param result
	 */
	public ResponseMessage(int code, org.springframework.http.HttpStatus status, String message, Object result) {
		this.code = code;
		this.status = status;
		this.message = message;
		this.result = result;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public org.springframework.http.HttpStatus getStatus() {
		return status;
	}

	public void setStatus(org.springframework.http.HttpStatus status) {
		this.status = status;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

}
