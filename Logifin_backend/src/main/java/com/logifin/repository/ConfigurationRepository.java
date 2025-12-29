package com.logifin.repository;

import com.logifin.entity.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

    /**
     * Find configuration by key
     */
    Optional<Configuration> findByConfigKey(String configKey);

    /**
     * Find active configuration by key
     */
    @Query("SELECT c FROM Configuration c WHERE c.configKey = :configKey AND c.isActive = true")
    Optional<Configuration> findActiveByConfigKey(@Param("configKey") String configKey);

    /**
     * Find all active configurations
     */
    List<Configuration> findByIsActiveTrue();

    /**
     * Find configurations by type
     */
    List<Configuration> findByConfigType(String configType);

    /**
     * Check if configuration key exists
     */
    boolean existsByConfigKey(String configKey);
}
