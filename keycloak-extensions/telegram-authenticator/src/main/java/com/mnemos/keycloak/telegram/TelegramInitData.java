package com.mnemos.keycloak.telegram;

import java.time.Instant;
import java.util.Map;

public record TelegramInitData(
        TelegramUser user,
        Instant authDate,
        String queryId,
        String hash,
        Map<String, String> parameters
) {
}