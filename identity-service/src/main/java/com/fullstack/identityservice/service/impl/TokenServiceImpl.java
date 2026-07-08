package com.fullstack.identityservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullstack.identityservice.dto.response.TokenResponse;
import com.fullstack.identityservice.model.Account;
import com.fullstack.identityservice.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public TokenServiceImpl(StringRedisTemplate redisTemplate,
                            ObjectMapper objectMapper,
                            @Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.access-token-minutes}") long accessMinutes,
                            @Value("${app.jwt.refresh-token-days}") long refreshDays) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTtl = Duration.ofMinutes(accessMinutes);
        this.refreshTtl = Duration.ofDays(refreshDays);
    }

    @Override
    public TokenResponse createSession(Account account) {
        String sid = UUID.randomUUID().toString();
        String refreshToken = randomToken();
        SessionRecord session = new SessionRecord();
        session.setSid(sid);
        session.setAccountId(account.getId());
        session.setUserId(account.getUserId());
        session.setEmail(account.getEmail());
        session.setRole(account.getRole().name());
        session.setRefreshTokenHash(sha256(refreshToken));
        saveSession(session);
        redisTemplate.opsForValue().set(refreshKey(session.getRefreshTokenHash()), sid, refreshTtl);
        return TokenResponse.builder()
                .accessToken(accessToken(account, sid))
                .refreshToken(refreshToken)
                .expiresInSeconds(accessTtl.toSeconds())
                .build();
    }

    @Override
    public TokenResponse rotateRefreshToken(String refreshToken) {
        String oldHash = sha256(refreshToken);
        String sid = redisTemplate.opsForValue().get(refreshKey(oldHash));
        if (sid == null) {
            throw new IllegalArgumentException("Refresh token is invalid or expired");
        }

        SessionRecord session = readSession(sid);
        if (!oldHash.equals(session.getRefreshTokenHash())) {
            revokeSession(sid);
            throw new IllegalArgumentException("Refresh token reuse detected");
        }

        String newRefreshToken = randomToken();
        session.setRefreshTokenHash(sha256(newRefreshToken));
        redisTemplate.delete(refreshKey(oldHash));
        saveSession(session);
        redisTemplate.opsForValue().set(refreshKey(session.getRefreshTokenHash()), sid, refreshTtl);

        AccountSnapshot account = new AccountSnapshot(session.getAccountId(), session.getUserId(),
                session.getEmail(), session.getRole());
        return TokenResponse.builder()
                .accessToken(accessToken(account, sid))
                .refreshToken(newRefreshToken)
                .expiresInSeconds(accessTtl.toSeconds())
                .build();
    }

    @Override
    public String accountIdByRefreshToken(String refreshToken) {
        String sid = redisTemplate.opsForValue().get(refreshKey(sha256(refreshToken)));
        if (sid == null) {
            throw new IllegalArgumentException("Refresh token is invalid or expired");
        }
        return readSession(sid).getAccountId();
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        String refreshHash = sha256(refreshToken);
        String sid = redisTemplate.opsForValue().get(refreshKey(refreshHash));
        if (sid != null) {
            revokeSession(sid);
        }
        try {
            blacklist(parse(accessToken));
        } catch (ExpiredJwtException ignored) {
            // Expired access tokens no longer need blacklisting; the refresh session is revoked above.
        }
    }

    private String accessToken(Account account, String sid) {
        return accessToken(new AccountSnapshot(account.getId(), account.getUserId(), account.getEmail(),
                account.getRole().name()), sid);
    }

    private String accessToken(AccountSnapshot account, String sid) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(account.userId()))
                .id(UUID.randomUUID().toString())
                .claim("sid", sid)
                .claim("accountId", account.accountId())
                .claim("email", account.email())
                .claim("role", account.role())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTtl)))
                .signWith(key)
                .compact();
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    private void blacklist(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration == null || claims.getId() == null) {
            return;
        }
        long seconds = Math.max(1, Duration.between(Instant.now(), expiration.toInstant()).toSeconds());
        redisTemplate.opsForValue().set("blacklist:access:" + claims.getId(), "true", Duration.ofSeconds(seconds));
    }

    private void saveSession(SessionRecord session) {
        try {
            redisTemplate.opsForValue().set(sessionKey(session.getSid()), objectMapper.writeValueAsString(session), refreshTtl);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private SessionRecord readSession(String sid) {
        String raw = redisTemplate.opsForValue().get(sessionKey(sid));
        if (raw == null) {
            throw new IllegalArgumentException("Session is invalid or expired");
        }
        try {
            return objectMapper.readValue(raw, SessionRecord.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void revokeSession(String sid) {
        String raw = redisTemplate.opsForValue().get(sessionKey(sid));
        if (raw != null) {
            try {
                redisTemplate.delete(refreshKey(objectMapper.readValue(raw, SessionRecord.class).getRefreshTokenHash()));
            } catch (JsonProcessingException ignored) {
                // session is being removed anyway
            }
        }
        redisTemplate.delete(sessionKey(sid));
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private String sessionKey(String sid) {
        return "session:" + sid;
    }

    private String refreshKey(String hash) {
        return "refresh:" + hash;
    }

    private record AccountSnapshot(String accountId, Long userId, String email, String role) {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SessionRecord {
        private String sid;
        private String accountId;
        private Long userId;
        private String email;
        private String role;
        private String refreshTokenHash;
    }
}
