package com.github.solenra.server.service.impl;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.solenra.server.entity.Identity;
import com.github.solenra.server.repository.IdentityRepository;

import java.util.Collections;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final IdentityRepository identityRepository;

    public AppUserDetailsService(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Identity identity = identityRepository.findByUsername(username);
        return new User(identity.getUsername(), identity.getPassword(), Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

}
