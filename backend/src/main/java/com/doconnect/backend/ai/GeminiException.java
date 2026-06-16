package com.doconnect.backend.ai;

public class GeminiException extends RuntimeException {

	private final Integer statusCode;

	public GeminiException(String message) {
		super(message);
		this.statusCode = null;
	}

	public GeminiException(String message, Throwable cause) {
		super(message, cause);
		this.statusCode = null;
	}

	public GeminiException(String message, Integer statusCode) {
		super(message);
		this.statusCode = statusCode;
	}

	public Integer getStatusCode() {
		return statusCode;
	}
}
