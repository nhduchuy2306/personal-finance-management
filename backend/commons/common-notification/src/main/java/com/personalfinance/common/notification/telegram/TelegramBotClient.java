package com.personalfinance.common.notification.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Telegram Bot API client.
 * Sends messages via HTTP API.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBotClient {

    private final TelegramProperties telegramProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Send a text message to a Telegram chat.
     *
     * @param chatId  Telegram chat ID
     * @param message Text message content
     * @return true if message was sent successfully
     */
    public boolean sendMessage(String chatId, String message) {
        try {
            String url = String.format("%s/bot%s/sendMessage",
                    telegramProperties.getApiBaseUrl(),
                    telegramProperties.getBotToken());

            Map<String, Object> body = Map.of(
                    "chat_id", chatId,
                    "text", message,
                    "parse_mode", "HTML"
            );

            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
            boolean success = response.getStatusCode().is2xxSuccessful();

            if (success) {
                log.debug("Telegram message sent to chat {}", chatId);
            } else {
                log.warn("Telegram message failed for chat {}: HTTP {}", chatId, response.getStatusCode());
            }
            return success;
        } catch (Exception e) {
            log.error("Failed to send Telegram message to {}: {}", chatId, e.getMessage(), e);
            return false;
        }
    }
}
