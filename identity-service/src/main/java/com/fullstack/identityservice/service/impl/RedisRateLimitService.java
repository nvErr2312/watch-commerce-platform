package com.fullstack.identityservice.service.impl;

import com.fullstack.identityservice.service.RateLimitService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisRateLimitService implements RateLimitService {
    private final StringRedisTemplate redisTemplate;

    @Override
    public void check(String key, int maxAttempts, long windowSeconds) {
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }
        if (attempts != null && attempts > maxAttempts) {
            throw new IllegalArgumentException("Too many attempts. Please try again later");
        }
    }
}
