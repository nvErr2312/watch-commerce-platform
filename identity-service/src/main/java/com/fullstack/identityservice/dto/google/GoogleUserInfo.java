package com.fullstack.identityservice.dto.google;

public record GoogleUserInfo(
        String subject,
        String email,
        String name,
        boolean emailVerified
) {
}
