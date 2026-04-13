package com.divyanshuagarwal.ratelimiter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.divyanshuagarwal.ratelimiter.model.RateLimiterResponse;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Service
public class RateLimiterService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final int MAX_TOKENS = 10;
    private static final double REFILL_RATE = 10.0 / 30.0; // tokens per second (full refill in 30s)

    public RateLimiterResponse allowRequest(String userId) {

        String key = "rate_limiter:" + userId;

        // Step 1: Get current time
        long currentTime = System.currentTimeMillis();

        // Step 2: Fetch tokens and lastRefillTime in a single Redis call
        List<Object> fields = redisTemplate.opsForHash().multiGet(key, Arrays.asList("tokens", "lastRefillTime"));

        double tokens;
        long lastRefillTime;

        // Step 3: Initialize if user not present
        if (fields.get(0) == null || fields.get(1) == null) {
            tokens = MAX_TOKENS;
            lastRefillTime = currentTime;
        } else {
            tokens = Double.parseDouble(fields.get(0).toString());
            lastRefillTime = Long.parseLong(fields.get(1).toString());
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

        // Step 6: Save tokens and lastRefillTime + set TTL in a single pipeline
        redisTemplate.executePipelined((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
            byte[] k = key.getBytes();
            connection.hSet(k, "tokens".getBytes(), String.valueOf(tokens).getBytes());
            connection.hSet(k, "lastRefillTime".getBytes(), String.valueOf(currentTime).getBytes());
            connection.expire(k, Duration.ofMinutes(2).getSeconds());
            return null;
        });

        return new RateLimiterResponse(true, tokens);
    }
}