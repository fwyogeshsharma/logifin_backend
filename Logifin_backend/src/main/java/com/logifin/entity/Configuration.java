package com.logifin.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Entity representing system configuration settings
 * Stores key-value pairs for portal behavior and business rules
 */
@Entity
@Table(name = "configurations", indexes = {
    @Index(name = "idx_config_key", columnList = "config_key"),
    @Index(name = "idx_config_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configuration extends BaseEntity {

    @NotBlank(message = "Config key is required")
    @Size(max = 100, message = "Config key must not exceed 100 characters")
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @NotBlank(message = "Config value is required")
    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;

    @NotBlank(message = "Config type is required")
    @Size(max = 50, message = "Config type must not exceed 50 characters")
    @Column(name = "config_type", nullable = false, length = 50)
    @Builder.Default
    private String configType = "STRING";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Is active flag is required")
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id", insertable = false, updatable = false)
    private User updatedBy;

    /**
     * Configuration type enumeration
     */
    public enum ConfigType {
        STRING,
        NUMBER,
        BOOLEAN,
        PERCENTAGE,
        JSON
    }
}
