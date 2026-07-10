package com.fullstack.commonservice.security.jwt;

import com.fullstack.commonservice.security.config.CommonSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
public class JwtTokenValidator {
    private final CommonSecurityProperties properties;

    public JwtClaims validate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new JwtClaims(
                claims.get("accountId", String.class),
                claims.getSubject(),
                claims.get("email", String.class),
                claims.getSubject(),
                claims.get("role", String.class)
        );
    }

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getJwtSecret()));
    }
}
