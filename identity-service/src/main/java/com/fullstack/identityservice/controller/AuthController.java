package com.fullstack.identityservice.controller;

import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.identityservice.dto.request.LoginRequest;
import com.fullstack.identityservice.dto.request.LogoutRequest;
import com.fullstack.identityservice.dto.request.RefreshRequest;
import com.fullstack.identityservice.dto.request.RegisterRequest;
import com.fullstack.identityservice.dto.response.RegisterResponse;
import com.fullstack.identityservice.dto.response.TokenResponse;
import com.fullstack.identityservice.service.IdentityService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
public class AuthController {
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
        return ResponseEntity.ok(new ResponseData<>("LOGIN_SUCCESS", "Login successfully",
                identityService.login(request)));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<ResponseData<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(new ResponseData<>("TOKEN_REFRESHED", "Token refreshed",
                identityService.refresh(request)));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<ResponseData<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        identityService.logout(request);
        return ResponseEntity.ok(new ResponseData<>("LOGOUT_SUCCESS", "Logout successfully", null));
    }
}
