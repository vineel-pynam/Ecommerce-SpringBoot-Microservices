package com.vineelpynam.PaymentService.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(
                        authHttpReq -> authHttpReq
                                .requestMatchers("/payment/**")
                                .hasAuthority("SCOPE_internal")
                                .anyRequest()
                                .authenticated()
                ).oauth2ResourceServer( oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }
}
