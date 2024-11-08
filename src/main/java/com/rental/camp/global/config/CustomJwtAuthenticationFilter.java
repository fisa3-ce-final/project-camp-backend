package com.rental.camp.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomJwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token == null) {
            //create guset
            Jwt guestJwt = new Jwt(
                    "guest-token", // 토큰 값
                    Instant.now(), // 발행 시간
                    Instant.MAX, // 매우 먼 미래로 만료 시간 설정
                    Map.of("alg", "none"), // 헤더
                    Map.of("sub", "guest",
                            "name", "Guest",
                            "email", "test@test.com",
                            "picture", "https://avatars.githubusercontent.com/u/124599?v=4")
            );

            Collection<GrantedAuthority> authorities = new HashSet<>(jwtGrantedAuthoritiesConverter.convert(guestJwt));
            authorities.add((GrantedAuthority) () -> "ROLE_USER");

            JwtAuthenticationToken guestAuthToken = new JwtAuthenticationToken(guestJwt, authorities);
            SecurityContextHolder.getContext().setAuthentication(guestAuthToken);
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
