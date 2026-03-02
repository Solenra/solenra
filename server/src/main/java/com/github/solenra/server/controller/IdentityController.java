package com.github.solenra.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import com.github.solenra.server.entity.Config;
import com.github.solenra.server.exceptions.ApplicationException;
import com.github.solenra.server.model.IdentityDto;
import com.github.solenra.server.service.ConfigService;
import com.github.solenra.server.service.IdentityService;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@RestController
@RequestMapping("/api/identity")
public class IdentityController {

    private static final Logger logger = LoggerFactory.getLogger(IdentityController.class);

    private final IdentityService identityService;
    private final ConfigService configService;

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public IdentityController(
            IdentityService identityService,
            ConfigService configService,
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder
    ) {
        this.identityService = identityService;
        this.configService = configService;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    @RequestMapping("/")
    public IdentityDto identity(Principal principal) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            // not logged in
            return null;
        };

        return new IdentityDto(principal.getName());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            HttpServletRequest httpRequest,
            @RequestParam("username") String username,
            @RequestParam("password") String password
    ) {
        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(username.toLowerCase(), password);
        Authentication auth = null;
        try {
            auth = authenticationManager.authenticate(authReq);
        } catch (BadCredentialsException e) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Bad credentials", "Bad credentials", e);
        }

        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, sc);

        // Create Access Token
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        Instant now = Instant.now();
        JwtClaimsSet accessClaims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .subject(username)
                .claim("roles", List.of("ROLE_USER"))
                .build();

        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, accessClaims)).getTokenValue();

        // Create Refresh Token
        JwtClaimsSet refreshClaims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(30, ChronoUnit.DAYS))
                .subject(username)
                .claim("type", "refresh")
                .build();

        String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), refreshClaims)).getTokenValue();

        logger.info("Issued JWT Access Token for {}", username);

        // TODO store tokens so they can be revoked

        return ResponseEntity.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        Jwt decoded;
        try {
            decoded = jwtDecoder.decode(refreshToken);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!"refresh".equals(decoded.getClaimAsString("type"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token type");
        }

        // TODO check the user still has access

        String username = decoded.getSubject();

        // Create Access Token
        Instant now = Instant.now();
        JwtClaimsSet accessClaims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .subject(decoded.getSubject())
                .claim("roles", List.of("ROLE_USER"))
                .build();

        String newAccessToken = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), accessClaims)).getTokenValue();

        logger.info("Issued refreshed JWT Access Token for {}", username);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @RequestMapping("/register")
    public IdentityDto register(Principal principal, @RequestBody IdentityDto identityDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            // already logged in
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Cannot register while logged in");
        };

        return identityService.register(identityDto);
    }

    // TODO delete user (except for admin)
    

    @RequestMapping("/terms-of-service")
    public Map<String, String> getTermsOfService() {
        Map<String, String> config = new HashMap<>();
        config.put("termsOfServiceHtml", configService.getConfigValue(Config.CODE_TERMS_OF_SERVICE_HTML));
        config.put("privacyPolicyHtml", configService.getConfigValue(Config.CODE_PRIVACY_POLICY_HTML));
        return config;
    }

}
