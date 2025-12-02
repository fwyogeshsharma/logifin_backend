package com.logifin.service;

import java.util.Set;

/**
 * Service interface for cache management operations.
 */
public interface CacheService {

    /**
     * Evict a specific entry from a cache.
     *
     * @param cacheName the name of the cache
     * @param key       the key to evict
     */
    void evict(String cacheName, Object key);

    /**
     * Evict all entries from a specific cache.
     *
     * @param cacheName the name of the cache
     */
    void evictAll(String cacheName);

    /**
     * Clear all caches.
     */
    void clearAllCaches();

    /**
     * Get all cache names.
     *
     * @return set of cache names
     */
    Set<String> getCacheNames();

    /**
     * Get cache statistics for monitoring.
     *
     * @param cacheName the name of the cache
     * @return cache statistics as a map
     */
    Object getCacheStats(String cacheName);

    /**
     * Warm up caches with frequently accessed data.
     */
    void warmUpCaches();

    /**
     * Evict all user-related caches.
     */
    void evictUserCaches();

    /**
     * Evict all role-related caches.
     */
    void evictRoleCaches();
}
