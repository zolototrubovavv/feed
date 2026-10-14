package com.github.zolototrubovavv.feed.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Initializing Security components -> Action: Configuring SecurityFilterChain bean");
        try {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/news", "/api/news/**").permitAll()
                    .requestMatchers("/api/news", "/api/news/**").authenticated()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()
                    .requestMatchers("/api/categories", "/api/categories/**").authenticated()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                    .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

            SecurityFilterChain chain = http.build();
            log.info("Successfully initialized Security components -> Type: SecurityFilterChain | Status: Endpoints secured, OAuth2 Resource Server enabled");
            return chain;
        } catch (Exception e) {
            log.error("Failed to initialize Security components -> Action: Filter chain assembly | Critical error", e);
            throw e;
        }
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null) {
                log.warn("Processing JWT token claims -> Context: realm_access extraction | Status: Missing claims in token");
                return Collections.emptyList();
            }
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles == null) {
                log.warn("Processing JWT token claims -> Context: roles extraction | Status: No roles found inside realm_access");
                return Collections.emptyList();
            }
            
            List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());

            log.debug("Processing JWT token claims -> Context: roles mapping | Status: Mapped {} authorities to SecurityContext", authorities.size());
            return authorities;
        });
        return jwtConverter;
    }
}