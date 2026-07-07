package com.fullstack.identityservice.service;

public interface RateLimitService {
    void check(String key, int maxAttempts, long windowSeconds);
}
