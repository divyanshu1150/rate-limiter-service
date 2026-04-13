package com.divyanshuagarwal.ratelimiter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.divyanshuagarwal.ratelimiter.model.RateLimiterResponse;

import java.time.Duration;

@Service
public class RateLimiterService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final int MAX_TOKENS = 100;
    private static final double REFILL_RATE = 100.0 / 60.0; // tokens per second

    public RateLimiterResponse allowRequest(String userId) {

        String key = "rate_limiter:" + userId;

        // Step 1: Get current time
        long currentTime = System.currentTimeMillis();

        // Step 2: Fetch from Redis
        Object tokensObj = redisTemplate.opsForHash().get(key, "tokens");
        Object lastRefillObj = redisTemplate.opsForHash().get(key, "lastRefillTime");

        double tokens;
        long lastRefillTime;

        // Step 3: Initialize if user not present
        if (tokensObj == null || lastRefillObj == null) {
            tokens = MAX_TOKENS;
            lastRefillTime = currentTime;
        } else {
            tokens = Double.parseDouble(tokensObj.toString());
            lastRefillTime = Long.parseLong(lastRefillObj.toString());
        }

        // Step 4: Refill tokens
        double timePassed = (currentTime - lastRefillTime) / 1000.0;
        tokens = Math.min(MAX_TOKENS, tokens + timePassed * REFILL_RATE);

        // Step 5: Allow or reject
        if (tokens < 1) {
            return new RateLimiterResponse(false, tokens);
        }

        // Consume token
        tokens -= 1;

        // Step 6: Save back to Redis
        redisTemplate.opsForHash().put(key, "tokens", String.valueOf(tokens));
        redisTemplate.opsForHash().put(key, "lastRefillTime", String.valueOf(currentTime));

        // Step 7: Set TTL (cleanup inactive users)
        redisTemplate.expire(key, Duration.ofMinutes(2));

        return new RateLimiterResponse(true, tokens);
    }
}