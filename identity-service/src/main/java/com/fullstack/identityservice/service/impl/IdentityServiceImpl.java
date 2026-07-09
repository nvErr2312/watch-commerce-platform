package com.fullstack.identityservice.service.impl;

import com.fullstack.commonservice.advice.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullstack.commonservice.notification.event.EmailVerificationRequestedEvent;
import com.fullstack.commonservice.service.KafkaService;
import com.fullstack.commonservice.user.command.CreateUserCommand;
import com.fullstack.commonservice.user.command.DeleteUserCommand;
import com.fullstack.commonservice.user.command.UpdateUserStatusCommand;
import com.fullstack.identityservice.dto.google.GoogleUserInfo;
import com.fullstack.identityservice.dto.request.GoogleLoginRequest;
import com.fullstack.identityservice.dto.request.LoginRequest;
import com.fullstack.identityservice.dto.request.LogoutRequest;
import com.fullstack.identityservice.dto.request.RefreshRequest;
import com.fullstack.identityservice.dto.request.RegisterRequest;
import com.fullstack.identityservice.dto.response.RegisterResponse;
import com.fullstack.identityservice.dto.response.TokenResponse;
import com.fullstack.identityservice.model.Account;
import com.fullstack.identityservice.model.AccountStatus;
import com.fullstack.identityservice.model.Role;
import com.fullstack.identityservice.repository.AccountRepository;
import com.fullstack.identityservice.service.IdentityService;
import com.fullstack.identityservice.service.GoogleTokenVerifier;
import com.fullstack.identityservice.service.RateLimitService;
import com.fullstack.identityservice.service.TokenService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class IdentityServiceImpl implements IdentityService {
    private final AccountRepository accountRepository;
    private final CommandGateway commandGateway;
    private final TokenService tokenService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final RateLimitService rateLimitService;
    private final StringRedisTemplate redisTemplate;
    private final KafkaService kafkaService;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.email-verification.ttl-minutes}")
    private long verificationTtlMinutes;

    @Value("${app.frontend.login-url}")
    private String loginUrl;

    @Value("${app.identity-service.base-url}")
    private String identityBaseUrl;

    @Value("${app.kafka.topics.email-verification}")
    private String emailVerificationTopic;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String username = request.getUsername().trim().toLowerCase();
        rateLimitService.check("rate:register:" + email, 5, 3600);
        if (accountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        //tạo userId trước
        Long userId = newUserId();

        //gửi command qua axon
        commandGateway.sendAndWait(new CreateUserCommand(
                userId,
                email,
                username,
                request.getFullName(),
                request.getPhone(),
                Role.USER.name(),
                AccountStatus.PENDING.name()));

        Account account = null;
        try {
            account = new Account();
            account.setEmail(email);
            account.setPassword(passwordEncoder.encode(request.getPassword()));
            account.setUserId(userId);
            account.setRole(Role.USER);
            account.setStatus(AccountStatus.PENDING);
            account = accountRepository.save(account);

            String verificationLink = createVerificationLink(account.getId());
            sendVerificationEmail(account.getEmail(), verificationLink);

            return RegisterResponse.builder()
                    .accountId(account.getId())
                    .userId(account.getUserId())
                    .email(account.getEmail())
                    .username(username)
                    .status(account.getStatus().name())
                    .build();
        } catch (RuntimeException exception) {
            if (account != null && account.getId() != null) {
                accountRepository.deleteById(account.getId());
            }
            try {
                commandGateway.sendAndWait(new DeleteUserCommand(userId));
            } catch (RuntimeException ignored) {
                // keep original failure visible
            }
            throw exception;
        }
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        String accountId = readVerificationToken(token);
        if (accountId == null) {
            throw new IllegalArgumentException("Verification token is invalid or expired");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setUpdatedAt(Instant.now());
        commandGateway.sendAndWait(new UpdateUserStatusCommand(account.getUserId(), AccountStatus.ACTIVE.name()));
        deleteVerificationToken(token);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        rateLimitService.check("rate:login:" + email, 10, 900);
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email or password is incorrect"));
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new IllegalArgumentException("Email or password is incorrect");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Please verify your email before logging in");
        }
        return tokenService.createSession(account);
    }

    @Override
    @Transactional
    public TokenResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleUserInfo googleUser = googleTokenVerifier.verify(request.getIdToken());
        if (!googleUser.emailVerified()) {
            throw new IllegalArgumentException("Google email is not verified");
        }

        String email = googleUser.email().trim().toLowerCase();
        rateLimitService.check("rate:google-login:" + email, 20, 900);

        Account account = accountRepository.findByEmail(email)
                .map(existing -> activateGoogleAccount(existing, googleUser))
                .orElseGet(() -> createGoogleAccount(googleUser));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not active");
        }
        return tokenService.createSession(account);
    }

    @Override
    public TokenResponse refresh(RefreshRequest request) {
        Account account = accountRepository.findById(tokenService.accountIdByRefreshToken(request.getRefreshToken()))
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not active");
        }
        return tokenService.rotateRefreshToken(request.getRefreshToken());
    }

    @Override
    public void logout(LogoutRequest request) {
        tokenService.logout(request.getAccessToken(), request.getRefreshToken());
    }

    private String createVerificationLink(String accountId) {
        String token = UUID.randomUUID().toString() + UUID.randomUUID();
        redisTemplate.opsForValue().set(verificationKey(token), accountId, Duration.ofMinutes(verificationTtlMinutes));
        return UriComponentsBuilder.fromUriString(identityBaseUrl)
                .path("/auth/verify-email")
                .queryParam("token", token)
                .toUriString();
    }

    private void sendVerificationEmail(String email, String verificationLink) {
        try {
            kafkaService.sendMessage(emailVerificationTopic,
                    objectMapper.writeValueAsString(new EmailVerificationRequestedEvent(email, verificationLink)));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not create email verification message", exception);
        }
    }

    private Account createGoogleAccount(GoogleUserInfo googleUser) {
        String email = googleUser.email().trim().toLowerCase();
        String username = email.substring(0, email.indexOf('@'));
        Long userId = newUserId();

        commandGateway.sendAndWait(new CreateUserCommand(
                userId,
                email,
                username,
                googleUser.name(),
                null,
                Role.USER.name(),
                AccountStatus.ACTIVE.name()));

        Account account = new Account();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        account.setUserId(userId);
        account.setRole(Role.USER);
        account.setStatus(AccountStatus.ACTIVE);
        return accountRepository.save(account);
    }

    private Account activateGoogleAccount(Account account, GoogleUserInfo googleUser) {
        if (account.getStatus() == AccountStatus.PENDING) {
            account.setStatus(AccountStatus.ACTIVE);
            account.setUpdatedAt(Instant.now());
            commandGateway.sendAndWait(new UpdateUserStatusCommand(account.getUserId(), AccountStatus.ACTIVE.name()));
        }
        return account;
    }

    private Long newUserId() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }

    private String readVerificationToken(String token) {
        return redisTemplate.opsForValue().get(verificationKey(token));
    }

    private void deleteVerificationToken(String token) {
        redisTemplate.delete(verificationKey(token));
    }

    private String verificationKey(String token) {
        return "email_verify:" + sha256(token);
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Override
    public String loginUrl() {
        return loginUrl;
    }
}
