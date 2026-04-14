package com.divyanshuagarwal.ratelimiter.model;

public class RateLimiterResponse {
    public boolean allowed;
    public double tokens;
    public long lastRefillTime;
    public int windowSeconds;

    public RateLimiterResponse(boolean allowed, double tokens, long lastRefillTime, int windowSeconds) {
        this.allowed = allowed;
        this.tokens = tokens;
        this.lastRefillTime = lastRefillTime;
        this.windowSeconds = windowSeconds;
    }
}
