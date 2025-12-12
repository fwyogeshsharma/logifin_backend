package com.logifin.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache configuration with Redis as the backing store.
 * Defines cache names, TTL settings, and serialization.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true", matchIfMissing = true)
public class CacheConfig extends CachingConfigurerSupport {

    // Cache Names
    public static final String CACHE_USERS = "users";
    public static final String CACHE_USER_BY_ID = "userById";
    public static final String CACHE_USER_BY_EMAIL = "userByEmail";
    public static final String CACHE_ROLES = "roles";
    public static final String CACHE_ROLE_BY_ID = "roleById";
    public static final String CACHE_ROLE_BY_NAME = "roleByName";
    public static final String CACHE_USER_DETAILS = "userDetails";
    public static final String CACHE_ACTIVE_USERS = "activeUsers";
    public static final String CACHE_USER_SEARCH = "userSearch";
    public static final String CACHE_COMPANIES = "companies";
    public static final String CACHE_COMPANY_BY_ID = "companyById";

    // Contract Module Cache Names
    public static final String CACHE_CONTRACTS = "contracts";
    public static final String CACHE_CONTRACT_BY_ID = "contractById";
    public static final String CACHE_CONTRACT_BY_NUMBER = "contractByNumber";
    public static final String CACHE_LOAN_STAGES = "loanStages";
    public static final String CACHE_LOAN_STAGE_BY_ID = "loanStageById";
    public static final String CACHE_LOAN_STAGE_BY_NAME = "loanStageByName";
    public static final String CACHE_CONTRACT_TYPES = "contractTypes";
    public static final String CACHE_CONTRACT_TYPE_BY_ID = "contractTypeById";
    public static final String CACHE_CONTRACT_TYPE_BY_NAME = "contractTypeByName";

    // Default TTL values (in minutes)
    @Value("${cache.ttl.default:60}")
    private long defaultTtlMinutes;

    @Value("${cache.ttl.users:30}")
    private long usersTtlMinutes;

    @Value("${cache.ttl.roles:120}")
    private long rolesTtlMinutes;

    @Value("${cache.ttl.user-details:15}")
    private long userDetailsTtlMinutes;

    @Value("${cache.ttl.search:5}")
    private long searchTtlMinutes;

    @Value("${cache.ttl.companies:30}")
    private long companiesTtlMinutes;

    @Value("${cache.ttl.contracts:30}")
    private long contractsTtlMinutes;

    @Value("${cache.ttl.loan-stages:120}")
    private long loanStagesTtlMinutes;

    @Value("${cache.ttl.contract-types:120}")
    private long contractTypesTtlMinutes;

    /**
     * Primary Redis Cache Manager with custom TTL per cache.
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(defaultTtlMinutes))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer()))
                .disableCachingNullValues();

        // Per-cache TTL configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // User caches - moderate TTL
        cacheConfigurations.put(CACHE_USERS, defaultConfig.entryTtl(Duration.ofMinutes(usersTtlMinutes)));
        cacheConfigurations.put(CACHE_USER_BY_ID, defaultConfig.entryTtl(Duration.ofMinutes(usersTtlMinutes)));
        cacheConfigurations.put(CACHE_USER_BY_EMAIL, defaultConfig.entryTtl(Duration.ofMinutes(usersTtlMinutes)));
        cacheConfigurations.put(CACHE_ACTIVE_USERS, defaultConfig.entryTtl(Duration.ofMinutes(usersTtlMinutes)));

        // Role caches - longer TTL (reference data changes rarely)
        cacheConfigurations.put(CACHE_ROLES, defaultConfig.entryTtl(Duration.ofMinutes(rolesTtlMinutes)));
        cacheConfigurations.put(CACHE_ROLE_BY_ID, defaultConfig.entryTtl(Duration.ofMinutes(rolesTtlMinutes)));
        cacheConfigurations.put(CACHE_ROLE_BY_NAME, defaultConfig.entryTtl(Duration.ofMinutes(rolesTtlMinutes)));

        // User details - shorter TTL for security
        cacheConfigurations.put(CACHE_USER_DETAILS, defaultConfig.entryTtl(Duration.ofMinutes(userDetailsTtlMinutes)));

        // Search results - very short TTL
        cacheConfigurations.put(CACHE_USER_SEARCH, defaultConfig.entryTtl(Duration.ofMinutes(searchTtlMinutes)));

        // Company caches - moderate TTL
        cacheConfigurations.put(CACHE_COMPANIES, defaultConfig.entryTtl(Duration.ofMinutes(companiesTtlMinutes)));
        cacheConfigurations.put(CACHE_COMPANY_BY_ID, defaultConfig.entryTtl(Duration.ofMinutes(companiesTtlMinutes)));

        // Contract caches - moderate TTL
        cacheConfigurations.put(CACHE_CONTRACTS, defaultConfig.entryTtl(Duration.ofMinutes(contractsTtlMinutes)));
        cacheConfigurations.put(CACHE_CONTRACT_BY_ID, defaultConfig.entryTtl(Duration.ofMinutes(contractsTtlMinutes)));
        cacheConfigurations.put(CACHE_CONTRACT_BY_NUMBER, defaultConfig.entryTtl(Duration.ofMinutes(contractsTtlMinutes)));

        // Loan Stage caches - longer TTL (reference data)
        cacheConfigurations.put(CACHE_LOAN_STAGES, defaultConfig.entryTtl(Duration.ofMinutes(loanStagesTtlMinutes)));
        cacheConfigurations.put(CACHE_LOAN_STAGE_BY_ID, defaultConfig.entryTtl(Duration.ofMinutes(loanStagesTtlMinutes)));
        cacheConfigurations.put(CACHE_LOAN_STAGE_BY_NAME, defaultConfig.entryTtl(Duration.ofMinutes(loanStagesTtlMinutes)));

        // Contract Type caches - longer TTL (reference data)
        cacheConfigurations.put(CACHE_CONTRACT_TYPES, defaultConfig.entryTtl(Duration.ofMinutes(contractTypesTtlMinutes)));
        cacheConfigurations.put(CACHE_CONTRACT_TYPE_BY_ID, defaultConfig.entryTtl(Duration.ofMinutes(contractTypesTtlMinutes)));
        cacheConfigurations.put(CACHE_CONTRACT_TYPE_BY_NAME, defaultConfig.entryTtl(Duration.ofMinutes(contractTypesTtlMinutes)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    /**
     * JSON serializer for cache values.
     */
    private GenericJackson2JsonRedisSerializer jsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    /**
     * Custom key generator for complex cache keys.
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName());
            sb.append(":");
            sb.append(method.getName());
            for (Object param : params) {
                sb.append(":");
                sb.append(param != null ? param.toString() : "null");
            }
            return sb.toString();
        };
    }

    /**
     * Custom error handler for cache failures.
     * Logs errors but allows the application to continue.
     */
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CustomCacheErrorHandler();
    }
}
