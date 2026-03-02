package com.github.solenra.server.exceptions;

import java.io.Serial;

public class HttpRequestException extends Exception {

	@Serial
	private static final long serialVersionUID = 1L;

	public HttpRequestException() {}

	public HttpRequestException(String message) {
		super(message);
	}

	public HttpRequestException(String message, Throwable cause) {
		super(message, cause);
	}

}
