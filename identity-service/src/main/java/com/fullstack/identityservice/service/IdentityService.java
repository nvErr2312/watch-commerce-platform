package com.fullstack.identityservice.service;

import com.fullstack.identityservice.dto.request.LoginRequest;
import com.fullstack.identityservice.dto.request.LogoutRequest;
import com.fullstack.identityservice.dto.request.RefreshRequest;
import com.fullstack.identityservice.dto.request.RegisterRequest;
import com.fullstack.identityservice.dto.response.RegisterResponse;
import com.fullstack.identityservice.dto.response.TokenResponse;

public interface IdentityService {
    RegisterResponse register(RegisterRequest request);
    void verifyEmail(String token);
    TokenResponse login(LoginRequest request);
    TokenResponse refresh(RefreshRequest request);
    void logout(LogoutRequest request);
    String loginUrl();
}
