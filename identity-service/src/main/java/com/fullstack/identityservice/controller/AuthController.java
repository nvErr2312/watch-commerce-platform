package com.fullstack.identityservice.controller;

import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.identityservice.dto.request.GoogleLoginRequest;
import com.fullstack.identityservice.dto.request.LoginRequest;
import com.fullstack.identityservice.dto.request.LogoutRequest;
import com.fullstack.identityservice.dto.request.RefreshRequest;
import com.fullstack.identityservice.dto.request.RegisterRequest;
import com.fullstack.identityservice.dto.response.RegisterResponse;
import com.fullstack.identityservice.dto.response.TokenResponse;
import com.fullstack.identityservice.service.IdentityService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_COOKIE = "__Host-refreshToken";

    private final IdentityService identityService;

    @PostMapping("/auth/register")
    public ResponseEntity<ResponseData<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData<>("ACCOUNT_REGISTERED", "Register successfully",
                        identityService.register(request)));
    }

    @GetMapping("/auth/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        identityService.verifyEmail(token);
        URI location = UriComponentsBuilder.fromUriString(identityService.loginUrl())
                .queryParam("verified", "true")
                .build()
                .toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResponseData<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokens = identityService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(tokens.getRefreshToken()).toString())
                .body(new ResponseData<>("LOGIN_SUCCESS", "Login successfully", tokens));
    }

    @PostMapping("/auth/google")
    public ResponseEntity<ResponseData<TokenResponse>> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        TokenResponse tokens = identityService.loginWithGoogle(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(tokens.getRefreshToken()).toString())
                .body(new ResponseData<>("LOGIN_SUCCESS", "Login successfully", tokens));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<ResponseData<TokenResponse>> refresh(
            @CookieValue(name = REFRESH_COOKIE) String refreshToken) {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken(refreshToken);
        TokenResponse tokens = identityService.refresh(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(tokens.getRefreshToken()).toString())
                .body(new ResponseData<>("TOKEN_REFRESHED", "Token refreshed", tokens));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<ResponseData<Void>> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        LogoutRequest request = new LogoutRequest();
        request.setAccessToken(bearerToken(authorization));
        request.setRefreshToken(refreshToken);
        identityService.logout(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(new ResponseData<>("LOGOUT_SUCCESS", "Logout successfully", null));
    }

    private ResponseCookie refreshCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
    }

    private String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }
}
