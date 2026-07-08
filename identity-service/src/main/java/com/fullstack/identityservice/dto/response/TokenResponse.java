package com.fullstack.identityservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
    private String accessToken;
    @JsonIgnore
    private String refreshToken;
    private long expiresInSeconds;
}
