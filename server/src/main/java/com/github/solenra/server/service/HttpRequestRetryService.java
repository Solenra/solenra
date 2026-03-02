package com.github.solenra.server.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.github.solenra.server.exceptions.HttpRequestException;
import com.github.solenra.server.exceptions.RateLimitExceededException;

import java.net.URI;

public interface HttpRequestRetryService {

	<T, U> ResponseEntity<T> doHttpRequest(RestClient restClient, String requestUrl, URI requestUri, HttpMethod httpMethod, Class<T> responseBodyType, String accessToken) throws HttpRequestException, RateLimitExceededException;

}
