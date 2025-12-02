package com.logifin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * Custom cache error handler that logs errors but allows the application to continue.
 * This ensures that cache failures don't break the application.
 */
@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.error("Cache GET error - cache: {}, key: {}, error: {}",
                cache.getName(), key, exception.getMessage());
        // Don't rethrow - allow application to continue without cache
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.error("Cache PUT error - cache: {}, key: {}, error: {}",
                cache.getName(), key, exception.getMessage());
        // Don't rethrow - allow application to continue without cache
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.error("Cache EVICT error - cache: {}, key: {}, error: {}",
                cache.getName(), key, exception.getMessage());
        // Don't rethrow - allow application to continue without cache
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.error("Cache CLEAR error - cache: {}, error: {}",
                cache.getName(), exception.getMessage());
        // Don't rethrow - allow application to continue without cache
    }
}
