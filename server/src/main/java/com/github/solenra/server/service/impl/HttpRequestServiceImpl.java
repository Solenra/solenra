package com.github.solenra.server.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.github.solenra.server.entity.Integration;
import com.github.solenra.server.entity.IntegrationAuthCredential;
import com.github.solenra.server.exceptions.HttpRequestException;
import com.github.solenra.server.service.HttpRequestRetryService;
import com.github.solenra.server.service.HttpRequestService;
import com.github.solenra.server.service.IntegrationAuthService;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.annotation.PostConstruct;

@Service("httpRequestService")
public class HttpRequestServiceImpl implements HttpRequestService {

	private final RestClient defaultRestClient;
	private final RestClient solaredgeV2RestClient;
	private final HttpRequestRetryService httpRequestRetryService;
	private final IntegrationAuthService integrationAuthService;

	@Value("${int.solaredge.api.base-url}")
	private String solaredgeApiUrl;

	@Value("${int.solaredgev2.api.base-url}")
	private String solaredgeV2ApiUrl;

	private List<String> rateLimitedUrls = Collections.emptyList();

	@PostConstruct
	private void initRateLimitedUrls() {
		rateLimitedUrls = new ArrayList<>();
		if (solaredgeApiUrl != null && !solaredgeApiUrl.isEmpty()) {
			rateLimitedUrls.add(solaredgeApiUrl);
		}
		if (solaredgeV2ApiUrl != null && !solaredgeV2ApiUrl.isEmpty()) {
			rateLimitedUrls.add(solaredgeV2ApiUrl);
		}
	}

	public HttpRequestServiceImpl(
			RestClient defaultRestClient,
			RestClient solaredgeV2RestClient,
			HttpRequestRetryService httpRequestRetryService,
			IntegrationAuthService integrationAuthService
	) {
		this.defaultRestClient = defaultRestClient;
		this.solaredgeV2RestClient = solaredgeV2RestClient;
		this.httpRequestRetryService = httpRequestRetryService;
		this.integrationAuthService = integrationAuthService;
	}

	@Override
	public <T, U> T doHttpRequest(HttpMethod httpMethod, String requestUrl, URI requestUri, Class<T> responseBodyType) throws HttpRequestException {
		return doHttpRequest(httpMethod, requestUrl, requestUri, responseBodyType, null);
	}

	@Override
	public <T, U> T doHttpRequest(HttpMethod httpMethod, String requestUrl, URI requestUri, Class<T> responseBodyType, String accessToken) throws HttpRequestException {
		ResponseEntity<T> response = null;

		boolean handleRateLimitRetry = false;
		RestClient restClient = null;

		String urlToCheck = requestUrl;
		if (requestUri != null) {
			urlToCheck = requestUri.toString();
		}

		if (urlToCheck.startsWith(solaredgeV2ApiUrl)) {
			restClient = solaredgeV2RestClient;
			String accountKey = integrationAuthService.getCredential(Integration.CODE_SOLAREDGE_V2, IntegrationAuthCredential.TYPE_ACCOUNT_KEY);
			solaredgeV2RestClient.get().header("X-Account-Key", accountKey);
		} else {
			restClient = defaultRestClient;
		}

		for (String rateLimitedUrl : rateLimitedUrls) {
            if (urlToCheck.startsWith(rateLimitedUrl)) {
                handleRateLimitRetry = true;
                break;
            }
		}

		if (handleRateLimitRetry) {
			response = httpRequestRetryService.doHttpRequest(restClient, requestUrl, requestUri, httpMethod, responseBodyType, accessToken);
		} else {
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
				if (accessToken != null) {
					// rethrow so access token can be refreshed
					throw e;
				}
				String errorMessage = "HTTP request error, httpMethod=" + httpMethod + ", requestUrl=" + requestUrl + ", body=" + e.getResponseBodyAsString();
				throw new HttpRequestException(errorMessage, e);
			}
		}

		// error if the http response code is not 2xx successful
		if (!response.getStatusCode().is2xxSuccessful()) {
			String errorMessage = "HTTP request failed, httpMethod=" + httpMethod + ", requestUrl=" + requestUrl + ", response status code=" + response.getStatusCode().value();
			throw new HttpRequestException(errorMessage);
		}

		return response.getBody();
	}

}
