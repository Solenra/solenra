package com.github.solenra.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import com.github.solenra.server.entity.*;
import com.github.solenra.server.entity.integration.SystemDetails;
import com.github.solenra.server.entity.integration.SystemEnergyDetails;
import com.github.solenra.server.exceptions.ApplicationException;
import com.github.solenra.server.exceptions.HttpRequestException;
import com.github.solenra.server.model.SystemDetailsDto;
import com.github.solenra.server.model.solaredge.Details;
import com.github.solenra.server.model.solaredge.EnergyDetails;
import com.github.solenra.server.model.solaredge.SiteOverview;
import com.github.solenra.server.model.solaredgev2.RefreshToken;
import com.github.solenra.server.model.solaredgev2.SiteDetails;
import com.github.solenra.server.repository.*;
import com.github.solenra.server.repository.integration.SystemEnergyDetailsRepository;
import com.github.solenra.server.service.HttpRequestService;
import com.github.solenra.server.service.IntegrationAuthService;
import com.github.solenra.server.service.SolaredgeApiService;
import com.github.solenra.server.service.SystemEnergyDetailsService;
import com.github.solenra.server.service.TransactionHelperService;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;

@Service("solaredgeApiService")
public class SolaredgeApiServiceImpl implements SolaredgeApiService {

    private static final Logger logger = LoggerFactory.getLogger(SolaredgeApiServiceImpl.class);

    private final SolarSystemIntegrationRepository solarSystemIntegrationRepository;
    private final SystemEnergyDetailsRepository systemEnergyDetailsRepository;
    private final SystemEnergyDetailsService systemEnergyDetailsService;
    private final HttpRequestService httpRequestService;
    private final IntegrationAuthService integrationAuthService;
    private final TransactionHelperService transactionHelperService;

	@Value("${int.solaredge.api.base-url}")
	private String solaredgeApiUrl;

	@Value("${int.solaredgev2.api.base-url}")
	private String solaredgeV2ApiUrl;

    public SolaredgeApiServiceImpl(
            SolarSystemIntegrationRepository solarSystemIntegrationRepository,
            SystemEnergyDetailsRepository systemEnergyDetailsRepository,
            SystemEnergyDetailsService systemEnergyDetailsService,
            HttpRequestService httpRequestService,
            IntegrationAuthService integrationAuthService,
            TransactionHelperService transactionHelperService
    ) {
        this.solarSystemIntegrationRepository = solarSystemIntegrationRepository;
        this.systemEnergyDetailsRepository = systemEnergyDetailsRepository;
        this.systemEnergyDetailsService = systemEnergyDetailsService;
        this.httpRequestService = httpRequestService;
        this.integrationAuthService = integrationAuthService;
        this.transactionHelperService = transactionHelperService;
    }

    @Override
    @Transactional(readOnly = true)
    public void runDataLoad(Long solarSystemIntegrationId) {
        logger.info("Running SolarEdge API data load for solarSystemIntegrationId [{}]", solarSystemIntegrationId);

        String siteId = integrationAuthService.getCredential(solarSystemIntegrationId, SolarSystemIntegrationAuthCredential.TYPE_SYSTEM_ID);
        runDataLoad(solaredgeApiUrl, solarSystemIntegrationId, siteId);

        logger.info("Finished SolarEdge API data load for solarSystemIntegrationId [{}]", solarSystemIntegrationId);
    }

    @Override
    @Transactional(readOnly = true)
    public void runDataLoadV2(long solarSystemIntegrationId) {
        logger.info("Running SolarEdgeV2 API data load for solarSystemIntegrationId [{}]", solarSystemIntegrationId);

        String siteId = integrationAuthService.getCredential(solarSystemIntegrationId, SolarSystemIntegrationAuthCredential.TYPE_SYSTEM_ID);
        runDataLoad(solaredgeV2ApiUrl, solarSystemIntegrationId, siteId);

        logger.info("Finished SolarEdgeV2 API data load for solarSystemIntegrationId [{}]", solarSystemIntegrationId);
    }

    private void runDataLoad(String baseUrl, long solarSystemIntegrationId, String siteId) {
        SolarSystemIntegration solarSystemIntegration = solarSystemIntegrationRepository.findById(solarSystemIntegrationId).orElseThrow(() -> {
            String errorMessage = "SolarSystemIntegration with ID [" + solarSystemIntegrationId + "] not found.";
            return new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        });

        SystemDetails systemDetails = solarSystemIntegration.getSystemDetails();
        SystemDetailsDto systemDetailsDto = null;
        if (systemDetails == null) {
            logger.debug("Fetching system details");

            if (!baseUrl.startsWith(solaredgeV2ApiUrl)) {
                String apiKey = integrationAuthService.getCredential(solarSystemIntegrationId, SolarSystemIntegrationAuthCredential.TYPE_API_KEY);
                String siteOverviewUrl = solaredgeApiUrl + "site/" + siteId + "/details.json?api_key=" + apiKey;
                Details.Root details = null;
                try {
                    details = doHttpRequest(HttpMethod.GET, siteOverviewUrl, null, Details.Root.class, solarSystemIntegrationId);
                } catch (Exception e) {
                    throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error requesting system details from SolarEdge API", e);
                }
                if (details != null) {
                    systemDetails = SystemDetails.convertToEntity(details);
                }
            } else {
                String siteOverviewUrl = solaredgeV2ApiUrl + "sites/" + siteId;
                SiteDetails details = null;
                try {
                    details = doHttpRequest(HttpMethod.GET, siteOverviewUrl, null, SiteDetails.class, solarSystemIntegrationId);
                } catch (Exception e) {
                    throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error requesting system details from SolarEdgeV2 API", e);
                }
                if (details != null) {
                    systemDetails = SystemDetails.convertToEntity(details, solarSystemIntegration);
                }
            }

            systemDetailsDto = systemEnergyDetailsService.saveSystemDetailsNewTransaction(systemDetails, solarSystemIntegration.getId());

            // TODO save in new transaction
            /*if (solarSystemIntegration.getTimezone() == null) {
                solarSystemIntegration.setTimezone(systemDetailsDto.getTimezone());
                solarSystemIntegration = solarSystemIntegrationRepository.save(solarSystemIntegration);
            }*/
        } else {
            systemDetailsDto = new SystemDetailsDto(systemDetails);
        }
        
        logger.debug("Processing system details {}", systemDetailsDto);

        ZoneId targetZone = ZoneId.of(solarSystemIntegration.getTimezone());
        ZonedDateTime processStartDate = systemDetailsDto.getInstallationDate().withZoneSameInstant(targetZone);
        ZonedDateTime currentDate = ZonedDateTime.now(targetZone);

        SystemEnergyDetails maxSystemEnergyDetails = systemEnergyDetailsRepository.findFirstBySolarSystemIntegrationOrderByEndDateDesc(solarSystemIntegration);

        if (maxSystemEnergyDetails != null) {
            processStartDate = maxSystemEnergyDetails.getEndDate().withZoneSameInstant(targetZone);///*.atZone(ZoneId.of("UTC"));
        }

        logger.debug("Process Start Date: {}", processStartDate);

        long energyDetailsMinutesDuration = 60; // TODO support configurable duration

        DateTimeFormatter energyDetailsDateTimeFormatter = null;
        if (!baseUrl.startsWith(solaredgeV2ApiUrl)) {
            energyDetailsDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        } else {
            energyDetailsDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnxxx");
        }

        String apiKey = integrationAuthService.getCredential(solarSystemIntegrationId, SolarSystemIntegrationAuthCredential.TYPE_API_KEY);
        UriComponentsBuilder solaredgeEnergyDetailsApiUriBuilder = UriComponentsBuilder.fromUriString(solaredgeApiUrl + "site/" + siteId + "/energyDetails.json")
                .queryParam("api_key", apiKey);
        UriComponentsBuilder solaredgeV2EnergyDetailsApiUriBuilder = UriComponentsBuilder.fromUriString(solaredgeV2ApiUrl + "sites/" + siteId + "/overview");

        if (processStartDate != null) {
            ZonedDateTime heartbeatZonedDateTime = ZonedDateTime.now();
            TemporalAmount heartbeatPeriod = java.time.Duration.ofMinutes(30);

            for (ZonedDateTime date = processStartDate; date.isBefore(currentDate.minusHours(1)); date = date.plusHours(1)) {
                if (ZonedDateTime.now().isAfter(heartbeatZonedDateTime.plus(heartbeatPeriod))) {
                    // update processing heartbeat
                    heartbeatZonedDateTime = ZonedDateTime.now();
                    transactionHelperService.saveSolarSystemIntegrationProcessingHeartbeat(solarSystemIntegration.getId(), heartbeatZonedDateTime);
                }

                logger.debug("Processing date: {}", date);

                SystemEnergyDetails systemEnergyDetails = null;

                if (!baseUrl.startsWith(solaredgeV2ApiUrl)) {
                    solaredgeEnergyDetailsApiUriBuilder = solaredgeEnergyDetailsApiUriBuilder
                            .replaceQueryParam("startTime", date.format(energyDetailsDateTimeFormatter))
                            .replaceQueryParam("endTime", date.plusHours(1).format(energyDetailsDateTimeFormatter))
                            .replaceQueryParam("timeUnit", "HOUR");
                    EnergyDetails.Root energyDetails = null;

                    try {
                        energyDetails = doHttpRequest(HttpMethod.GET, null, solaredgeEnergyDetailsApiUriBuilder.build().toUri(), EnergyDetails.Root.class, solarSystemIntegrationId);
                    } catch (Exception e) {
                        throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error requesting energy details from SolarEdge API with URL [" + solaredgeEnergyDetailsApiUriBuilder.build().toUriString() + "]", e);
                    }
                    if (energyDetails != null) {
                        systemEnergyDetails = SystemEnergyDetails.convertToEntity(energyDetails);
                    }
                } else {
                    solaredgeV2EnergyDetailsApiUriBuilder = solaredgeV2EnergyDetailsApiUriBuilder
                            .replaceQueryParam("from", URLEncoder.encode(date.format(energyDetailsDateTimeFormatter), StandardCharsets.UTF_8))
                            .replaceQueryParam("to", URLEncoder.encode(date.plusHours(1).format(energyDetailsDateTimeFormatter), StandardCharsets.UTF_8));
                    SiteOverview siteOverview = null;

                    try {
                        siteOverview = doHttpRequest(HttpMethod.GET, null, solaredgeV2EnergyDetailsApiUriBuilder.build(true).toUri(), SiteOverview.class, solarSystemIntegrationId);
                    } catch (Exception e) {
                        throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error requesting site overview from SolarEdge API with URL [" + solaredgeV2EnergyDetailsApiUriBuilder.build().toUriString() + "]", e);
                    }
                    if (siteOverview != null) {
                        systemEnergyDetails = SystemEnergyDetails.convertToEntity(siteOverview);
                    }
                }

                if (systemEnergyDetails != null) {
                    systemEnergyDetailsService.saveSystemEnergyDetailsNewTransaction(solarSystemIntegration.getId(), systemEnergyDetails, date/*.toLocalDateTime()*/, energyDetailsMinutesDuration);
                }
            }
        }
    }

    private <T> T doHttpRequest(HttpMethod httpMethod, String requestUrl, URI requestUri, Class<T> responseBodyType, long solarSystemIntegrationId) throws HttpRequestException {
        // TODO use OAuth 2.0 client
        T data = null;

        String urlToCheck = requestUrl;
        if (requestUri != null) {
            urlToCheck = requestUri.toString();
        }
        if (!urlToCheck.startsWith(solaredgeV2ApiUrl)) {
            data = httpRequestService.doHttpRequest(HttpMethod.GET, requestUrl, requestUri, responseBodyType);
        } else {
            // get credentials
            String accessToken = integrationAuthService.getAccessToken(solarSystemIntegrationId, false);

            if (accessToken == null) {
                // TODO error / reset integration
                // TODO set status to expired
                return null;
            }

            try {
                data = httpRequestService.doHttpRequest(httpMethod, requestUrl, requestUri, responseBodyType, accessToken);
            } catch (HttpClientErrorException e) {
                // 401 Unauthorized: "{"error_description":"The access token is invalid or has expired","error":"invalid_token"}"
                if (HttpStatus.UNAUTHORIZED.value() == e.getStatusCode().value()) {
                    RefreshToken refreshTokenError = e.getResponseBodyAs(RefreshToken.class);
                    if ("invalid_token".equals(refreshTokenError.getError())) {
                        // refresh token
                        logger.debug("Attemping token refresh for solarSystemIntegrationId [{}]", solarSystemIntegrationId);
                        accessToken = integrationAuthService.getAccessToken(solarSystemIntegrationId, true);

                        try {
                            data = httpRequestService.doHttpRequest(httpMethod, requestUrl, requestUri, responseBodyType, accessToken);
                        } catch (Exception e2) {
                            String errorMessage = "HTTP request error, httpMethod=" + httpMethod + ", requestUrl: " + requestUrl + ", requestUri=" + requestUri + ", body=" + e.getResponseBodyAsString();
                            throw new HttpRequestException(errorMessage, e);
                        }
                    }
                } else {
                    throw e;
                }
            }
        }

        return data;
    }

}
