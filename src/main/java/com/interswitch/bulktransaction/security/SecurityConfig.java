package com.interswitch.bulktransaction.security;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    private final JwtTokenProvider jwtProvider;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/v1/authenticate").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/submit/bulk-transactions").hasRole("ROLE_USER")
                        .pathMatchers(HttpMethod.GET, "/api/v1/bulk-transactions/**").hasRole("ROLE_ADMIN")
                        .pathMatchers("/actuator/health").hasRole("ROLE_ADMIN")
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private AuthenticationWebFilter jwtWebFilter() {
        AuthenticationWebFilter webFilter = new AuthenticationWebFilter(jwtReactiveAuthManager());
        webFilter.setServerAuthenticationConverter(bearerConverter());
        return webFilter;
    }

    private ReactiveAuthenticationManager jwtReactiveAuthManager() {
        return authentication -> Mono.just(authentication);
    }

    private ServerAuthenticationConverter bearerConverter() {
        return exchange -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) return Mono.empty();
            String token = authHeader.substring(7);

            try {
                var claims = jwtProvider.validateToken(token).getBody();
                List<SimpleGrantedAuthority> authorities = ((List<?>) claims.get("roles")).stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, authorities);
                return Mono.just(auth);
            } catch (JwtException e) {
                return Mono.empty();
            }
        };
    }
}
