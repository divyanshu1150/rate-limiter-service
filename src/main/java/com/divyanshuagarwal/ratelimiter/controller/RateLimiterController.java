package com.divyanshuagarwal.ratelimiter.controller;

import com.divyanshuagarwal.ratelimiter.model.RateLimiterResponse;
import com.divyanshuagarwal.ratelimiter.service.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rate-limit")
public class RateLimiterController {

    @Autowired
    private RateLimiterService rateLimiterService;

    @GetMapping("/allow")
    public ResponseEntity<RateLimiterResponse> allow(@RequestParam String userId) {
        RateLimiterResponse response = rateLimiterService.allowRequest(userId);

        if (response.allowed) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(429).body(response);
        }
    }
}