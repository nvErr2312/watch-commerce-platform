package com.fullstack.identityservice.service;

import com.fullstack.identityservice.dto.response.TokenResponse;
import com.fullstack.identityservice.model.Account;

public interface TokenService {
    TokenResponse createSession(Account account);
    TokenResponse rotateRefreshToken(String refreshToken);
    String accountIdByRefreshToken(String refreshToken);
    void logout(String accessToken, String refreshToken);
}
