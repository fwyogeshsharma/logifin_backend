package com.logifin.service.impl;

import com.logifin.config.CacheConfig;
import com.logifin.service.CacheService;
import com.logifin.service.RoleService;
import com.logifin.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of CacheService for Redis cache management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheServiceImpl implements CacheService {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    // Lazy injection to avoid circular dependency
    private UserService userService;
    private RoleService roleService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public void evict(String cacheName, Object key) {
        log.debug("Evicting cache entry - cache: {}, key: {}", cacheName, key);
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.info("Cache entry evicted - cache: {}, key: {}", cacheName, key);
        } else {
            log.warn("Cache not found: {}", cacheName);
        }
    }

    @Override
    public void evictAll(String cacheName) {
        log.debug("Evicting all entries from cache: {}", cacheName);
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("All entries evicted from cache: {}", cacheName);
        } else {
            log.warn("Cache not found: {}", cacheName);
        }
    }

    @Override
    public void clearAllCaches() {
        log.info("Clearing all caches");
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.debug("Cleared cache: {}", cacheName);
            }
        });
        log.info("All caches cleared");
    }

    @Override
    public Set<String> getCacheNames() {
        return cacheManager.getCacheNames().stream().collect(Collectors.toSet());
    }

    @Override
    public Object getCacheStats(String cacheName) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheName", cacheName);

        try {
            // Get Redis keys for this cache
            Set<String> keys = redisTemplate.keys(cacheName + "::*");
            stats.put("keyCount", keys != null ? keys.size() : 0);
            stats.put("keys", keys);
        } catch (Exception e) {
            log.error("Error getting cache stats for {}: {}", cacheName, e.getMessage());
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    @Override
    @Async
    public void warmUpCaches() {
        log.info("Starting cache warm-up");

        try {
            // Warm up roles cache (reference data - doesn't change often)
            if (roleService != null) {
                log.debug("Warming up roles cache");
                roleService.getAllRoles();
            }

            // Warm up active users cache
            if (userService != null) {
                log.debug("Warming up active users cache");
                userService.getActiveUsers();
            }

            log.info("Cache warm-up completed");
        } catch (Exception e) {
            log.error("Error during cache warm-up: {}", e.getMessage());
        }
    }

    @Override
    public void evictUserCaches() {
        log.info("Evicting all user-related caches");
        evictAll(CacheConfig.CACHE_USERS);
        evictAll(CacheConfig.CACHE_USER_BY_ID);
        evictAll(CacheConfig.CACHE_USER_BY_EMAIL);
        evictAll(CacheConfig.CACHE_ACTIVE_USERS);
        evictAll(CacheConfig.CACHE_USER_SEARCH);
        evictAll(CacheConfig.CACHE_USER_DETAILS);
    }

    @Override
    public void evictRoleCaches() {
        log.info("Evicting all role-related caches");
        evictAll(CacheConfig.CACHE_ROLES);
        evictAll(CacheConfig.CACHE_ROLE_BY_ID);
        evictAll(CacheConfig.CACHE_ROLE_BY_NAME);
    }
}
