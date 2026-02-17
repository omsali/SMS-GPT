package com.aioversms.queryservice.sms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final StringRedisTemplate redisTemplate;
    
    // Key format: session:phoneNumber
    private static final String KEY_PREFIX = "session:";

    public void savePages(String phoneNumber, List<String> pages) {
        String key = KEY_PREFIX + phoneNumber;
        
        // Clear old session
        redisTemplate.delete(key);

        // Push all pages to a Redis List (Right Push)
        if (pages.size() > 1) {
            for (int i = 1; i < pages.size(); i++) {
                redisTemplate.opsForList().rightPush(key, pages.get(i));
            }
            // Set expiry (e.g., 1 hour)
            redisTemplate.expire(key, Duration.ofHours(1));
        }
    }

    public String getNextPage(String phoneNumber) {
        String key = KEY_PREFIX + phoneNumber;
        // Pop the next page from the left (FIFO)
        return redisTemplate.opsForList().leftPop(key);
    }
}