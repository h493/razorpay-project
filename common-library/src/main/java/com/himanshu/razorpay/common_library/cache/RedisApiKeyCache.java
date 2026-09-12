package com.himanshu.razorpay.common_library.cache;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisApiKeyCache implements ApiKeyCache {

    private static final String API_KEY_CACHE_PREFIX = "apiKey:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ApiKeyCacheEntry> get(String keyId) {

        try{
            String json = stringRedisTemplate.opsForValue().get(API_KEY_CACHE_PREFIX + keyId);
            log.info("Fetched API key from cache for keyId {}: {}", keyId, json);
            if(json == null){
                return Optional.empty();
            }
            ApiKeyCacheEntry entry = objectMapper.readValue(json, ApiKeyCacheEntry.class);
            log.info("Retrieved API key from cache for keyId {}: {}", keyId, entry);
            return Optional.of(entry);
        }catch (Exception e) {
            log.warn("Api Key cache read failed, keyId: {}", keyId);
            return Optional.empty();
        }
    }

    @Override
    public void put(String keyId, ApiKeyCacheEntry entry) {
        try {
            stringRedisTemplate.opsForValue()
                    .set(API_KEY_CACHE_PREFIX + keyId, objectMapper.writeValueAsString(entry), TTL);
        }catch (Exception e){
            log.warn("Api Key cache write failed, keyId: {}", keyId);
        }

    }

    @Override
    public void evict(String keyId) {
        try {
            stringRedisTemplate.delete(API_KEY_CACHE_PREFIX + keyId);
        }catch (Exception e){
            log.warn("Api Key cache eviction failed, keyId: {}", keyId);
        }
    }
}
