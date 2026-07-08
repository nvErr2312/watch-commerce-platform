package com.fullstack.commonservice.security.jwt;

public record JwtClaims(
        String accountId,
        String userId,
        String email,
        String username,
        String role
) {}
