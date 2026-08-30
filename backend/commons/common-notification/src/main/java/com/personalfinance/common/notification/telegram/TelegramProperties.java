package com.personalfinance.common.notification.telegram;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Telegram Bot configuration properties.
 * Configured via application.yml: app.telegram.*
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.telegram")
public class TelegramProperties {

  /**
   * Telegram Bot token
   */
  private String botToken;

  /**
   * Telegram Bot API base URL
   */
  private String apiBaseUrl = "https://api.telegram.org";
}
