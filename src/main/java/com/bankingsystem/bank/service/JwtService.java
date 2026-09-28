package com.bankingsystem.bank.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service 
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final long jwtExpiration;

    public JwtService (
        JwtEncoder jwtEncoder,
        @Value("${jwt.expiration}") long jwtExpiration
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtExpiration = jwtExpiration;

    }

    public String generateToken(Authentication authentication) {
        String userId = authentication.getName();
        
        String role = authentication.getAuthorities()
            .stream()
            .findFirst()
            .map(authority -> authority.getAuthority())
            .orElseThrow();
        
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(userId)
            .claim("role", role)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusMillis(jwtExpiration))
            .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(claims);
        
        String token = jwtEncoder.encode(parameters).getTokenValue();
        
        return token;
    }
}
