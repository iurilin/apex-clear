package com.apex.clear_engine.application.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    private static final long KEY_TTL_MINUTES = 5;

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryReserveKey(String key) {
        String redisKey = "idempotency:" + key;

        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                redisKey,
                "PENDING",
                KEY_TTL_MINUTES,
                TimeUnit.MINUTES
        );

        return success != null && success;
    }

    public void confirmKey(String key, String responseBody) {
        String redisKey = "idempotency:" + key;
        redisTemplate.opsForValue().set(redisKey, "SUCCESS:" + responseBody, KEY_TTL_MINUTES, TimeUnit.MINUTES);
    }

    public void deleteKey(String key) {
        String redisKey = "idempotency:" + key;
        redisTemplate.delete(redisKey);
    }

    public String getKeyState(String key) {
        return redisTemplate.opsForValue().get("idempotency:" + key);
    }
}