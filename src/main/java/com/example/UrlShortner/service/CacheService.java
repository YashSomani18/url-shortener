package com.example.UrlShortner.service;

import com.example.UrlShortner.model.Url;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String URL_CACHE_PREFIX = "url:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    
    public void cacheUrl(String shortCode, Url url) {
        String key = URL_CACHE_PREFIX + shortCode;
        try {
            redisTemplate.opsForValue().set(key, url, CACHE_TTL);
            log.debug("Cached URL: {}", shortCode);
        } catch (Exception e) {
            log.error("Failed to cache URL: {}", shortCode, e);
        }
    }
    
    public Optional<Url> getUrlFromCache(String shortCode) {
        String key = URL_CACHE_PREFIX + shortCode;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof Url) {
                log.debug("Cache hit for URL: {}", shortCode);
                return Optional.of((Url) cached);
            }
        } catch (Exception e) {
            log.error("Failed to get URL from cache: {}", shortCode, e);
        }
        return Optional.empty();
    }
    
    public void removeUrlFromCache(String shortCode) {
        String key = URL_CACHE_PREFIX + shortCode;
        try {
            redisTemplate.delete(key);
            log.debug("Removed URL from cache: {}", shortCode);
        } catch (Exception e) {
            log.error("Failed to remove URL from cache: {}", shortCode, e);
        }
    }
    
    public void clearAllUrlCache() {
        try {
            Set<String> keys = redisTemplate.keys(URL_CACHE_PREFIX + "*");
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Cleared all URL cache entries");
            }
        } catch (Exception e) {
            log.error("Failed to clear URL cache", e);
        }
    }
    
    public boolean isUrlCached(String shortCode) {
        String key = URL_CACHE_PREFIX + shortCode;
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Failed to check if URL is cached: {}", shortCode, e);
            return false;
        }
    }
} 