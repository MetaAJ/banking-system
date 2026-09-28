package com.bankingsystem.bank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import com.bankingsystem.bank.service.CustomUserDetailsService;

import org.springframework.beans.factory.annotation.Value;

@Configuration 
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean 
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
        new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean 
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
        throws Exception {
            return configuration.getAuthenticationManager();
    }

    @Bean 
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/customers")
                .hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/accounts")
                .hasRole("CUSTOMER")
            .requestMatchers(HttpMethod.POST, "/api/accounts/*/deposit")
                .hasRole("CUSTOMER")
            .requestMatchers(HttpMethod.POST, "/api/accounts/*/withdraw")
                .hasRole("CUSTOMER")
            .requestMatchers(HttpMethod.POST, "/api/accounts/transfer")
                .hasRole("CUSTOMER")
            .requestMatchers(HttpMethod.POST, "/api/admin/accounts")
                .hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
        ));

        return http.build();
    }

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKey =
            new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );

        return NimbusJwtDecoder
            .withSecretKey(secretKey)
            .build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKeySpec secretKey =
            new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );

        return NimbusJwtEncoder
            .withSecretKey(secretKey)
            .build();
    }

    @Bean 
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");
            return List.of(new SimpleGrantedAuthority(role));
        });

        return converter;
    }
}
