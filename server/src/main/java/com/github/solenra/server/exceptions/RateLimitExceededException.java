package com.github.solenra.server.exceptions;

import java.io.Serial;

public class RateLimitExceededException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public RateLimitExceededException() {}

	public RateLimitExceededException(String message) {
		super(message);
	}

	public RateLimitExceededException(String message, Throwable cause) {
		super(message, cause);
	}

}
