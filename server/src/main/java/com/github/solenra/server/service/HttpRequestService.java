package com.github.solenra.server.service;

import org.springframework.http.HttpMethod;

import com.github.solenra.server.exceptions.HttpRequestException;

import java.net.URI;

public interface HttpRequestService {

    <T, U> T doHttpRequest(HttpMethod httpMethod, String requestUrl, URI requestUri, Class<T> responseBodyType) throws HttpRequestException;

    <T, U> T doHttpRequest(HttpMethod httpMethod, String requestUrl, URI requestUri, Class<T> responseBodyType, String accessToken) throws HttpRequestException;

}
