package com.github.solenra.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.github.solenra.server.exceptions.HttpRequestException;
import com.github.solenra.server.exceptions.RateLimitExceededException;
import com.github.solenra.server.service.HttpRequestRetryService;

import java.net.URI;

@Service("httpRequestRetryService")
public class HttpRequestRetryServiceImpl implements HttpRequestRetryService {

	private static final Logger logger = LoggerFactory.getLogger(HttpRequestRetryServiceImpl.class);

	@Override
	@Retryable(retryFor = RateLimitExceededException.class, maxAttempts = 24, backoff = @Backoff(random = true, delay = 240000, maxDelay = 480000, multiplier = 1))
	public <T, U> ResponseEntity<T> doHttpRequest(RestClient restClient, String requestUrl, URI requestUri, HttpMethod httpMethod, Class<T> responseBodyType, String accessToken) throws HttpRequestException {
		ResponseEntity<T> response = null;

		try {
			if (requestUri != null) {
				if (accessToken != null) {
					response = restClient
							.get()
							.uri(requestUri)
							.header("Authorization", "Bearer " + accessToken)
							.retrieve()
							.toEntity(responseBodyType);
				} else {
					response = restClient.get().uri(requestUri).retrieve().toEntity(responseBodyType);
				}
			} else {
				if (accessToken != null) {
					response = restClient
							.get()
							.uri(requestUrl)
							.header("Authorization", "Bearer " + accessToken)
							.retrieve()
							.toEntity(responseBodyType);
				} else {
					response = restClient.get().uri(requestUrl).retrieve().toEntity(responseBodyType);
				}
			}
		} catch (RestClientResponseException e) {
			if (HttpStatus.TOO_MANY_REQUESTS.value() == e.getStatusCode().value()) {
				// error if rate limit exceeded
				String errorMessage = "HTTP request rate limit exceeded, backing off.";
				logger.warn(errorMessage);
				throw new RateLimitExceededException(errorMessage, e);
			}

			if (accessToken != null) {
				// rethrow so access token can be refreshed
				throw e;
			}

			String errorMessage = "HTTP request error, httpMethod=" + httpMethod + ", requestUrl=" + requestUrl + ", body=" + e.getResponseBodyAsString();
			throw new HttpRequestException(errorMessage, e);
		}

		return response;
	}

}
