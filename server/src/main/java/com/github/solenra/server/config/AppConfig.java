package com.github.solenra.server.config;

import jakarta.annotation.Resource;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableSpringDataWebSupport(
        pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO
)
@EnableRetry
public class AppConfig {

    @Resource
    private Environment env;

    private RestClient getRestClient(HttpHeaders defaultHeaders, String basicAuthUsername, String basicAuthPassword) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(15));
        factory.setConnectionRequestTimeout(Duration.ofSeconds(15));
        HttpClientBuilder clientBuilder = HttpClientBuilder.create().disableAutomaticRetries();

        // use proxy if configured
        String proxyUseSystemProperties = env.getProperty("http.proxy.use-system-properties");
        String proxyHostname = env.getProperty("http.proxy.hostname");
        String proxyPort = env.getProperty("http.proxy.port");

        if ("true".equals(proxyUseSystemProperties)) {
            clientBuilder.useSystemProperties();
        } else if (StringUtils.hasLength(proxyHostname) && StringUtils.hasLength(proxyPort)) {
            int port = Integer.parseInt(proxyPort);
            HttpHost proxyHost = new HttpHost(proxyHostname, port);
            clientBuilder.setProxy(proxyHost).disableCookieManagement().disableAutomaticRetries();

            String proxyUsername = env.getProperty("http.proxy.username");
            String proxyPassword = env.getProperty("http.proxy.password");

            if (StringUtils.hasLength(proxyUsername) && StringUtils.hasLength(proxyPassword)) {
                BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(
                        new AuthScope(proxyHostname, port),
                        new UsernamePasswordCredentials(proxyUsername, proxyPassword.toCharArray())
                );
                clientBuilder.setDefaultCredentialsProvider(credsProvider);
            }
        }

        CloseableHttpClient httpClient = clientBuilder.build();
        factory.setHttpClient(httpClient);

        RestClient.Builder restClientBuilder = RestClient.builder()
                .requestFactory(factory);

        if (StringUtils.hasLength(basicAuthUsername) && StringUtils.hasLength(basicAuthPassword)) {
            restClientBuilder = restClientBuilder
                    .requestInterceptor(new BasicAuthenticationInterceptor(basicAuthUsername, basicAuthPassword));
        }

        if (defaultHeaders != null) {
            // set default headers
            restClientBuilder = restClientBuilder.requestInterceptor((request, body, execution) -> {
                request.getHeaders().addAll(defaultHeaders);
                return execution.execute(request, body);
            });
        }

        return restClientBuilder.build();
    }

    @Bean
    public RestClient defaultRestClient() {
        return getRestClient(null, null, null);
    }

    @Bean
    public RestClient solaredgeV2RestClient() {
        return getRestClient(null, null, null);
    }

}