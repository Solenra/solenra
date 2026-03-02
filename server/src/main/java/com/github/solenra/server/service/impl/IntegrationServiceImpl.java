package com.github.solenra.server.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.github.solenra.server.entity.Integration;
import com.github.solenra.server.entity.IntegrationAuthCredential;
import com.github.solenra.server.exceptions.ApplicationException;
import com.github.solenra.server.model.IntegrationDto;
import com.github.solenra.server.repository.IntegrationAuthCredentialRepository;
import com.github.solenra.server.repository.IntegrationRepository;
import com.github.solenra.server.service.IntegrationService;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("integrationService")
public class IntegrationServiceImpl implements IntegrationService {

    private final IntegrationAuthCredentialRepository integrationAuthCredentialRepository;

    private final IntegrationRepository integrationRepository;
    
    public IntegrationServiceImpl(
        IntegrationRepository integrationRepository,
        IntegrationAuthCredentialRepository integrationAuthCredentialRepository
    ) {
        this.integrationRepository = integrationRepository;
        this.integrationAuthCredentialRepository = integrationAuthCredentialRepository;
    }

    @Override
    public List<IntegrationDto> getIntegrations(Principal principal) {
        // TODO permission check
        List<Integration> integrations = integrationRepository.findAll();
        return integrations.stream().map(IntegrationDto::new).collect(Collectors.toList());
    }

    @Override
    public void saveIntegration(Principal principal, Map<String, Object> integrationData) {
        // TODO permission check

        boolean doSave = false;

        String code = String.valueOf(integrationData.get("code"));

        Integration integration = integrationRepository.findByCode(code);
        if (integration == null) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Integration with code [" + code + "] not found.");
        }

        Object v = integrationData.get("enabled");
        Boolean enabled = null;
        if (v instanceof Boolean) {
            enabled = (Boolean) v;
        } else if (v instanceof Number) {
            enabled = ((Number) v).intValue() != 0;
        } else if (v != null) {
            enabled = Boolean.parseBoolean(v.toString().trim());
        }

        if (enabled != null && enabled != integration.getEnabled()) {
            doSave = true;
            integration.setEnabled(enabled);
        }

        List<String> allCredentialFields = Arrays.asList(
            IntegrationAuthCredential.TYPE_ACCOUNT_KEY,
            IntegrationAuthCredential.TYPE_CLIENT_ID,
            IntegrationAuthCredential.TYPE_CLIENT_SECRET
        );
        
        for (String credentialField : allCredentialFields) {
            if (integrationData.containsKey(credentialField)) {
                String accountKey = String.valueOf(integrationData.get(credentialField));
                
                IntegrationAuthCredential integrationAuthCredential = integration.getIntegrationAuthCredentials().stream()
                    .filter(cred -> credentialField.equals(cred.getType()) && !Objects.equals(accountKey, cred.getValue()))
                    .findFirst()
                    .orElse(null);

                if (integrationAuthCredential != null) {
                    integrationAuthCredential.setValue(accountKey);
                    integrationAuthCredential = integrationAuthCredentialRepository.save(integrationAuthCredential);
                }
            }
        }

        if (doSave) {
            integration = integrationRepository.save(integration);
        }
    }

}
