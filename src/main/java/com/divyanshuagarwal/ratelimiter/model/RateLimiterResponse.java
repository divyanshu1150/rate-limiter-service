package com.divyanshuagarwal.ratelimiter.model;

public class RateLimiterResponse {
    public boolean allowed;
    public double tokens;

    public RateLimiterResponse(boolean allowed, double tokens) {
        this.allowed = allowed;
        this.tokens = tokens;
    }
}
