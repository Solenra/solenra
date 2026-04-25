package com.github.solenra.server.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.solenra.server.entity.SolarSystemIntegrationAuthCredential;
import com.github.solenra.server.entity.SolarSystemIntegrationStatus;
import com.github.solenra.server.service.IntegrationAuthService;
import com.github.solenra.server.service.SolarSystemService;
import com.github.solenra.server.service.TransactionHelperService;

import java.util.Arrays;

@Controller
@RequestMapping(value={"/integration"})
public class IntegrationAuthController {

    private final IntegrationAuthService integrationAuthService;
    private final SolarSystemService solarSystemService;
    private final TransactionHelperService transactionHelperService;

    @Value("${BASE_URL}")
    private String baseUrl;

    public IntegrationAuthController(
            IntegrationAuthService integrationAuthService,
            SolarSystemService solarSystemService,
            TransactionHelperService transactionHelperService
    ) {
        this.integrationAuthService = integrationAuthService;
        this.solarSystemService = solarSystemService;
        this.transactionHelperService = transactionHelperService;
    }

    @RequestMapping("/oauth/solaredge/")
    public ResponseEntity<Object> oauthSolaredge(
            @RequestParam(name = "site_id") String siteId,
            @RequestParam(name = "code") String code,
            @RequestParam(name = "external_id") Long solarSystemIntegrationId
    ) {
        // https://developers.solaredge.com/docs/monitoring/kjnkktv8jwj0h-solar-edge-connect-o-auth-2-0#step-by-step-tutorial

        // TODO do not connect if integration status is in progress/processing

        integrationAuthService.saveCredential(solarSystemIntegrationId, SolarSystemIntegrationAuthCredential.TYPE_SYSTEM_ID, siteId);
        integrationAuthService.saveCredential(solarSystemIntegrationId, SolarSystemIntegrationAuthCredential.TYPE_AUTH_CODE, code);
        integrationAuthService.deleteCredentials(solarSystemIntegrationId, Arrays.asList(SolarSystemIntegrationAuthCredential.TYPE_ACCESS_TOKEN, SolarSystemIntegrationAuthCredential.TYPE_REFRESH_TOKEN));
        transactionHelperService.saveSolarSystemIntegrationStatus(solarSystemIntegrationId, SolarSystemIntegrationStatus.CODE_PENDING, null);

        Long solarSystemId = solarSystemService.getSolarSystemIdByIntegration(solarSystemIntegrationId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", baseUrl + "/solar-systems/" + solarSystemId);

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

}
