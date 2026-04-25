package com.example.bookmark_saver.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.bookmark_saver.utility.CommaSeparatedParser;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableWebSecurity
@EnableAutoConfiguration(exclude = OAuth2ResourceServerAutoConfiguration.class)
public class SecurityConfig {
    
    @Value("${auth.enabled:false}")
    private boolean authEnabled;

    @Value("${cors.origins}")
    private String corsOrigins;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.audiences:}")
    private String audiences;

    @Value("${spring.security.oauth2.resourceserver.roles-claim:}")
    private String rolesClaim;

    @PostConstruct
    public void validate() {
        if (!authEnabled) return;

        if (issuerUri.isBlank() && jwkSetUri.isBlank()) {
            throw new IllegalArgumentException(
                "Missing required auth configuration: define at least one of " +
                "spring.security.oauth2.resourceserver.jwt.issuer-uri (recommended, full OIDC discovery) or " +
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri (signature validation only)"
            );
        }

        Map<String, String> required = Map.of(
            "spring.security.oauth2.resourceserver.jwt.audiences", audiences,
            "spring.security.oauth2.resourceserver.roles-claim", rolesClaim
        );

        required.forEach((property, value) -> {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(
                    "Missing required auth configuration: " + property
                );
            }
        });
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        if (!authEnabled) {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

            return http.build();
        }

        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(
                auth -> auth
                    .requestMatchers("/public/**").permitAll()
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer(
                oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    @ConditionalOnProperty(
        name = "auth.enabled",
        havingValue = "true",
        matchIfMissing = false
    )
    public JwtDecoder jwtDecoder() {
        return !issuerUri.isBlank()
            ? JwtDecoders.fromIssuerLocation(issuerUri)
            : NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    /**
     * Returns the CORS configuration.
     */
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(CommaSeparatedParser.parse(corsOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // for cookie/JWT

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", config);

        return source;
    }

    /**
     * Converts JWT roles into Spring Security granted authorities.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
            jwt -> {
                Collection<GrantedAuthority> authorities = new ArrayList<>();

                Object roles = extractClaim(jwt.getClaims());

                if (roles instanceof Collection<?> roleList) {
                    roleList.stream()
                        .filter(String.class::isInstance)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .forEach(authorities::add);
                }

                return authorities;
            }
        );

        return converter;
    }

    /**
     * Extracts a nested value from JWT claims.
     */
    private Object extractClaim(Map<String, Object> claims) {
        Object cursor = claims;

        for (String key : rolesClaim.split("\\.")) {
            if (cursor instanceof Map<?, ?> map) {
                cursor = map.get(key);
            } else {
                return null;
            }
        }

        return cursor;
    }
}