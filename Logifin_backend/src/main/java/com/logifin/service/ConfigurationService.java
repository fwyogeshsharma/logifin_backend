package com.logifin.service;

import com.logifin.dto.ConfigurationDTO;

import java.math.BigDecimal;
import java.util.List;

public interface ConfigurationService {

    /**
     * Get all configurations
     */
    List<ConfigurationDTO> getAllConfigurations();

    /**
     * Get configuration by key
     */
    ConfigurationDTO getConfigurationByKey(String configKey);

    /**
     * Get configuration value as string
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * Get configuration value as BigDecimal
     */
    BigDecimal getConfigValueAsDecimal(String configKey, BigDecimal defaultValue);

    /**
     * Get configuration value as Boolean
     */
    Boolean getConfigValueAsBoolean(String configKey, Boolean defaultValue);

    /**
     * Get configuration value as Integer
     */
    Integer getConfigValueAsInteger(String configKey, Integer defaultValue);

    /**
     * Create or update configuration
     */
    ConfigurationDTO saveConfiguration(ConfigurationDTO configurationDTO, Long userId);

    /**
     * Delete configuration
     */
    void deleteConfiguration(Long id);

    /**
     * Get portal service charge percentage
     */
    BigDecimal getPortalServiceChargePercentage();
}
