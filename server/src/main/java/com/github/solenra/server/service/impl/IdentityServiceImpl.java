package com.github.solenra.server.service.impl;

import org.springframework.stereotype.Service;

import com.github.solenra.server.model.IdentityDto;
import com.github.solenra.server.service.IdentityService;

@Service("identityService")
public class IdentityServiceImpl implements IdentityService {

    @Override
    public IdentityDto register(IdentityDto identityDto) {
        // TODO trim and lowercase where appropriate

        throw new UnsupportedOperationException("Unimplemented method 'register'");
    }

}
