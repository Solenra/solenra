package com.github.solenra.server.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.github.solenra.server.entity.Integration;
import com.github.solenra.server.entity.IntegrationAuthCredential;
import com.github.solenra.server.entity.SolarSystemIntegration;
import com.github.solenra.server.entity.SolarSystemIntegrationAuthCredential;
import com.github.solenra.server.exceptions.ApplicationException;
import com.github.solenra.server.model.solaredgev2.AuthCode;
import com.github.solenra.server.model.solaredgev2.BearerToken;
import com.github.solenra.server.model.solaredgev2.RefreshToken;
import com.github.solenra.server.repository.IntegrationAuthCredentialRepository;
import com.github.solenra.server.repository.SolarSystemIntegrationAuthCredentialRepository;
import com.github.solenra.server.repository.SolarSystemIntegrationRepository;
import com.github.solenra.server.service.IntegrationAuthService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service("integrationAuthService")
public class IntegrationAuthServiceImpl implements IntegrationAuthService {

    private final IntegrationAuthCredentialRepository integrationAuthCredentialRepository;
    private final SolarSystemIntegrationRepository solarSystemIntegrationRepository;
    private final SolarSystemIntegrationAuthCredentialRepository solarSystemIntegrationAuthCredentialRepository;

    private final RestClient solaredgeV2RestClient;

    public IntegrationAuthServiceImpl(
        IntegrationAuthCredentialRepository integrationAuthCredentialRepository,
        SolarSystemIntegrationRepository solarSystemIntegrationRepository,
        SolarSystemIntegrationAuthCredentialRepository solarSystemIntegrationAuthCredentialRepository,
        RestClient solaredgeV2RestClient
    ) {
        this.integrationAuthCredentialRepository = integrationAuthCredentialRepository;
        this.solarSystemIntegrationRepository = solarSystemIntegrationRepository;
        this.solarSystemIntegrationAuthCredentialRepository = solarSystemIntegrationAuthCredentialRepository;
        this.solaredgeV2RestClient = solaredgeV2RestClient;
    }

    private SolarSystemIntegration getSolarSystemIntegration(Long id) {
        return solarSystemIntegrationRepository.findById(id).orElseThrow(() -> {
            String errorMessage = "SolarSystemIntegration with ID [" + id + "] not found.";
            return new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        });
    }

    @Override
    public String getCredential(String integrationCode, String type) {
        // TODO cache for 5min and evict if changed
        IntegrationAuthCredential integrationAuthCredential = integrationAuthCredentialRepository.findByIntegrationCodeAndType(integrationCode, type);

        if (integrationAuthCredential != null) {
            return integrationAuthCredential.getValue();
        }

        return null;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCredential(Long solarSystemIntegrationId, String type, String value) {
        SolarSystemIntegration solarSystemIntegration = null;
        SolarSystemIntegrationAuthCredential credential = solarSystemIntegrationAuthCredentialRepository.findBySolarSystemIntegrationIdAndType(solarSystemIntegrationId, type);

        if (credential == null) {
            solarSystemIntegration = getSolarSystemIntegration(solarSystemIntegrationId);
            credential = new SolarSystemIntegrationAuthCredential();
            credential.setSolarSystemIntegration(solarSystemIntegration);
            credential.setType(type);
        }

        credential.setValue(value);

        credential = solarSystemIntegrationAuthCredentialRepository.save(credential);

        if (SolarSystemIntegrationAuthCredential.TYPE_AUTH_CODE.equals(type)) {
            // enable the integration now it is connected
            if (solarSystemIntegration == null) {
                solarSystemIntegration = getSolarSystemIntegration(solarSystemIntegrationId);
            }

            solarSystemIntegration.setEnabled(true);
            solarSystemIntegration = solarSystemIntegrationRepository.save(solarSystemIntegration);
        }
    }

    @Override
    @Transactional
    public void deleteCredentials(Long solarSystemIntegrationId, List<String> typeCodes) {
        solarSystemIntegrationAuthCredentialRepository.deleteAllBySolarSystemIntegrationIdAndTypeIn(solarSystemIntegrationId, typeCodes);
    }

    @Override
    public Map<String, String> getCredentials(long solarSystemIntegrationId, List<String> types) {
        Map<String, String> credentials = new HashMap<>();

        List<SolarSystemIntegrationAuthCredential> solarSystemIntegrationAuthCredentials = solarSystemIntegrationAuthCredentialRepository.findAllBySolarSystemIntegrationIdAndTypeIn(solarSystemIntegrationId, types);

        for (SolarSystemIntegrationAuthCredential solarSystemIntegrationAuthCredential : solarSystemIntegrationAuthCredentials) {
            credentials.put(solarSystemIntegrationAuthCredential.getType(), solarSystemIntegrationAuthCredential.getValue());
        }

        return credentials;
    }

    @Override
    public String getCredential(long solarSystemIntegrationId, String type) {
        SolarSystemIntegrationAuthCredential solarSystemIntegrationAuthCredential = solarSystemIntegrationAuthCredentialRepository.findBySolarSystemIntegrationIdAndType(solarSystemIntegrationId, type);

        if (solarSystemIntegrationAuthCredential != null) {
            return solarSystemIntegrationAuthCredential.getValue();
        }

        return null;
    }

    @Override
    public String getAccessToken(long solarSystemIntegrationId, boolean forceRefresh) {
        String accessToken = getCredential(solarSystemIntegrationId, SolarSystemIntegrationAuthCredential.TYPE_ACCESS_TOKEN);

        if (accessToken == null || forceRefresh) {
            String refreshToken = getCredential(solarSystemIntegrationId, SolarSystemIntegrationAuthCredential.TYPE_REFRESH_TOKEN);

            String clientId = getCredential(Integration.CODE_SOLAREDGE_V2, IntegrationAuthCredential.TYPE_CLIENT_ID);
            String clientSecret = getCredential(Integration.CODE_SOLAREDGE_V2, IntegrationAuthCredential.TYPE_CLIENT_SECRET);

            BearerToken bearerTokenResult = null;

            boolean skipRefreshToken = refreshToken == null;

            if (!skipRefreshToken) {
                // get new access token using refresh token

                // get bearer token

                // POST to https://monitoringapi.solaredge.com/v2/oauth2/token

                RefreshToken refreshTokenPost = new RefreshToken();
                refreshTokenPost.setGrantType("refresh_token");
                refreshTokenPost.setRefreshToken(refreshToken);
                refreshTokenPost.setClientId(clientId);
                refreshTokenPost.setClientSecret(clientSecret);

                try {
                    bearerTokenResult = solaredgeV2RestClient.post()
                            .uri("https://monitoringapi.solaredge.com/v2/oauth2/token")
                            .body(refreshTokenPost)
                            .retrieve()
                            .body(BearerToken.class);
                } catch (HttpClientErrorException e) {
                    boolean throwException = true;
                    if (HttpStatus.BAD_REQUEST.value() == e.getStatusCode().value()) {
                        RefreshToken refreshTokenError = e.getResponseBodyAs(RefreshToken.class);
                        if (refreshTokenError != null && "invalid_request".equals(refreshTokenError.getError())) {
                            skipRefreshToken = true;
                            throwException = false;
                        }
                    }
                    if (throwException) {
                        throw e;
                    }
                }
            }

            if (skipRefreshToken) {
                // authenticate with auth code
                String authCode = getCredential(solarSystemIntegrationId, SolarSystemIntegrationAuthCredential.TYPE_AUTH_CODE);

                if (authCode == null) {
                    // TODO throw exception, reset integration to be re-authorised
                    return null;
                }

                // get bearer token

                // POST to https://monitoringapi.solaredge.com/v2/oauth2/token

                AuthCode authCodePost = new AuthCode();
                authCodePost.setGrantType("authorization_code");
                authCodePost.setCode(authCode);
                authCodePost.setClientId(clientId);
                authCodePost.setClientSecret(clientSecret);

                try {
                bearerTokenResult = solaredgeV2RestClient.post()
                        .uri("https://monitoringapi.solaredge.com/v2/oauth2/token")
                        .body(authCodePost)
                        .retrieve()
                        .body(BearerToken.class);
                } catch (HttpClientErrorException e) {
                    boolean throwException = true;
                    if (HttpStatus.BAD_REQUEST.value() == e.getStatusCode().value()) {
                        RefreshToken refreshTokenError = e.getResponseBodyAs(RefreshToken.class);
                        if (refreshTokenError != null && "invalid_request".equals(refreshTokenError.getError())) {
                            // integration credentials are no longer valid, need to be re-authorised
                            // TODO reset integration to be re-authorised
                            // TODO send email notification
                            throw new CredentialsExpiredException("Invalid request error received when attempting to refresh token, credentials expired.", e);
                        }
                    }
                    if (throwException) {
                        throw e;
                    }
                }
            }

            if (bearerTokenResult == null) {
                throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting token from SolarEdge V2 API");
            }

            if (StringUtils.hasLength(bearerTokenResult.getError())) {
                // TODO
            } else {
                if (StringUtils.hasLength(bearerTokenResult.getAccessToken())) {
                    accessToken = bearerTokenResult.getAccessToken();
                    saveCredential(solarSystemIntegrationId, SolarSystemIntegrationAuthCredential.TYPE_ACCESS_TOKEN, accessToken);
                }
                if (StringUtils.hasLength(bearerTokenResult.getRefreshToken())) {
                    saveCredential(solarSystemIntegrationId, SolarSystemIntegrationAuthCredential.TYPE_REFRESH_TOKEN, bearerTokenResult.getRefreshToken());
                }
                if (!Objects.isNull(bearerTokenResult.getExpiresIn()) && bearerTokenResult.getExpiresIn().compareTo(0L) > 0) {
                    // TODO
                }
            }
        }

        return accessToken;
    }

    // TODO use cache only if setting ttl to match expiresIn value

}
