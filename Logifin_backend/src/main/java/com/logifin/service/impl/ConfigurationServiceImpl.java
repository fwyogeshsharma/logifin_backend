package com.logifin.service.impl;

import com.logifin.dto.ConfigurationDTO;
import com.logifin.entity.Configuration;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.ConfigurationRepository;
import com.logifin.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "configurations", key = "'all'")
    public List<ConfigurationDTO> getAllConfigurations() {
        return configurationRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "configurations", key = "#configKey")
    public ConfigurationDTO getConfigurationByKey(String configKey) {
        Configuration config = configurationRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration not found with key: " + configKey));
        return mapToDTO(config);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "configValues", key = "#configKey")
    public String getConfigValue(String configKey, String defaultValue) {
        return configurationRepository.findActiveByConfigKey(configKey)
                .map(Configuration::getConfigValue)
                .orElse(defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "configValues", key = "#configKey + '_decimal'")
    public BigDecimal getConfigValueAsDecimal(String configKey, BigDecimal defaultValue) {
        try {
            String value = getConfigValue(configKey, null);
            return value != null ? new BigDecimal(value) : defaultValue;
        } catch (NumberFormatException e) {
            log.error("Error parsing config value as decimal for key: {}", configKey, e);
            return defaultValue;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "configValues", key = "#configKey + '_boolean'")
    public Boolean getConfigValueAsBoolean(String configKey, Boolean defaultValue) {
        String value = getConfigValue(configKey, null);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "configValues", key = "#configKey + '_integer'")
    public Integer getConfigValueAsInteger(String configKey, Integer defaultValue) {
        try {
            String value = getConfigValue(configKey, null);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            log.error("Error parsing config value as integer for key: {}", configKey, e);
            return defaultValue;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"configurations", "configValues"}, allEntries = true)
    public ConfigurationDTO saveConfiguration(ConfigurationDTO dto, Long userId) {
        Configuration config;

        if (dto.getId() != null) {
            // Update existing configuration
            config = configurationRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Configuration not found with ID: " + dto.getId()));
            config.setConfigValue(dto.getConfigValue());
            config.setConfigType(dto.getConfigType());
            config.setDescription(dto.getDescription());
            config.setIsActive(dto.getIsActive());
            config.setUpdatedByUserId(userId);
        } else {
            // Create new configuration
            if (configurationRepository.existsByConfigKey(dto.getConfigKey())) {
                throw new DuplicateResourceException("Configuration already exists with key: " + dto.getConfigKey());
            }

            config = Configuration.builder()
                    .configKey(dto.getConfigKey())
                    .configValue(dto.getConfigValue())
                    .configType(dto.getConfigType())
                    .description(dto.getDescription())
                    .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                    .createdByUserId(userId)
                    .build();
        }

        config = configurationRepository.save(config);
        log.info("Configuration saved: {}", config.getConfigKey());
        return mapToDTO(config);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"configurations", "configValues"}, allEntries = true)
    public void deleteConfiguration(Long id) {
        if (!configurationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Configuration not found with ID: " + id);
        }
        configurationRepository.deleteById(id);
        log.info("Configuration deleted with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "configValues", key = "'portal_service_charge'")
    public BigDecimal getPortalServiceChargePercentage() {
        return getConfigValueAsDecimal("portal_service_charge", new BigDecimal("0.5"));
    }

    private ConfigurationDTO mapToDTO(Configuration config) {
        return ConfigurationDTO.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .configType(config.getConfigType())
                .description(config.getDescription())
                .isActive(config.getIsActive())
                .createdByUserId(config.getCreatedByUserId())
                .updatedByUserId(config.getUpdatedByUserId())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
