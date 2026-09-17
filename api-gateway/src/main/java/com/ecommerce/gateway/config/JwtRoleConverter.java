package com.ecommerce.gateway.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.Collections;

public class JwtRoleConverter
        implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {

        String role = jwt.getClaimAsString("role");

        if (role == null || role.isBlank()) {

            return Mono.just(
                    new JwtAuthenticationToken(
                            jwt,
                            Collections.emptyList()
                    )
            );
        }

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + role);

        return Mono.just(
                new JwtAuthenticationToken(
                        jwt,
                        Collections.singletonList(authority)
                )
        );
    }
}