package com.personalfinance.auth.features.systemconfig.model;

import com.personalfinance.common.base.entity.BaseEntity;
import com.personalfinance.common.cache.enums.ConfigName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * System-wide configuration entity.
 * Stores dynamic config as key-value pairs (ConfigName → String value).
 * Owned by auth-service — other services read config via Redis (SystemConfigReader).
 */
@Entity
@Table(name = "system_config")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(name = "config_name", nullable = false, unique = true, length = 100)
  private ConfigName configName;

  @Column(name = "value", nullable = false, length = 1000)
  private String value;

  @Column(name = "description", length = 500)
  private String description;
}
