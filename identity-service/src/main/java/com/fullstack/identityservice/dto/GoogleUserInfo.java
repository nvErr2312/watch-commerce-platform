package com.fullstack.identityservice.dto;

public record GoogleUserInfo(
        String subject,
        String email,
        String name,
        boolean emailVerified
) {
}
