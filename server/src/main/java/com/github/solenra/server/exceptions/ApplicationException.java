package com.github.solenra.server.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class ApplicationException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	private HttpStatus httpStatus;
	private String userMessage;
	private String errorMessage;

	/**
	 * Constructs a new exception with null as its error message.
	 */
	public ApplicationException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified user message
	 *
	 * @param httpStatus the http status error code to return to the client.
	 * @param message the error message to return to the client.
	 */
	public ApplicationException(HttpStatus httpStatus, String message) {
		super(message);
		this.httpStatus = httpStatus;
		this.userMessage = message;
		this.errorMessage = message;
	}

	/**
	 * Constructs a new exception with the specified user message
	 *
	 * @param httpStatus the http status error code to return to the client.
	 * @param userMessage the error message to return to the client.
	 * @param errorMessage the error message to log.
	 */
	public ApplicationException(HttpStatus httpStatus, String userMessage, String errorMessage) {
		super(errorMessage);
		this.httpStatus = httpStatus;
		this.userMessage = userMessage;
		this.errorMessage = errorMessage;
	}

	/**
	 * Constructs a new exception with the specified user message and cause.
	 *
	 * @param httpStatus httpStatusCode the http status error code to return to the client.
	 * @param message the error message to return to the client.
	 * @param cause the original error message, used in exception chaining.
	 */
	public ApplicationException(HttpStatus httpStatus, String message, Throwable cause) {
		super(message, cause);
		this.httpStatus = httpStatus;
		this.userMessage = message;
		this.errorMessage = message;
	}

	/**
	 * Constructs a new exception with the specified user message and cause.
	 *
	 * @param httpStatus httpStatusCode the http status error code to return to the client.
	 * @param userMessage the error message to return to the client.
	 * @param errorMessage the error message to log.
	 * @param cause the original error message, used in exception chaining.
	 */
	public ApplicationException(HttpStatus httpStatus, String userMessage, String errorMessage, Throwable cause) {
		super(errorMessage, cause);
		this.httpStatus = httpStatus;
		this.userMessage = userMessage;
		this.errorMessage = errorMessage;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}

	public String getUserMessage() {
		return userMessage;
	}

	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
