package com.rental.camp.global.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Extract authorities from the JWT token
        Collection<GrantedAuthority> authorities = new HashSet<>(jwtGrantedAuthoritiesConverter.convert(jwt));

        // Cognito 그룹 정보를 권한으로 추가
        List<String> groups = jwt.getClaimAsStringList("cognito:groups");
        if (groups != null) {
            for (String group : groups) {
                authorities.add((GrantedAuthority) () -> "ROLE_" + group);
            }
        }

        // Create and return the authentication token
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
