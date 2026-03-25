package com.zbs.de.model.dto;

public class DtoEventBookingValidationResult {
	private boolean allowed;
	private String message;

	public DtoEventBookingValidationResult(boolean allowed, String message) {
		this.allowed = allowed;
		this.message = message;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public String getMessage() {
		return message;
	}
}