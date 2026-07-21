package com.mnemos.keycloak.telegram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class TelegramInitDataValidator {
    
    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final byte[] WEB_APP_DATA = "WebAppData".getBytes(StandardCharsets.UTF_8);
    
    private static final Logger LOGGER = Logger.getLogger(TelegramInitDataValidator.class);
    
    private final String botToken;
    private final Duration maximumAge;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    
    public TelegramInitDataValidator(String botToken, Duration maximumAge) {
        this(botToken, maximumAge, new ObjectMapper(), Clock.systemUTC());
    }
    
    TelegramInitDataValidator(String botToken, Duration maximumAge, ObjectMapper objectMapper, Clock clock) {
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalArgumentException("Telegram bot token must not be blank");
        }
        
        if (maximumAge == null || maximumAge.isNegative() || maximumAge.isZero()) {
            throw new IllegalArgumentException("Maximum initData age must be positive");
        }
        
        this.botToken = botToken;
        this.maximumAge = maximumAge;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }
    
    public Optional<TelegramInitData> validate(String rawInitData) {
        if (rawInitData == null || rawInitData.isBlank()) {
            LOGGER.warn("Telegram initData validation failed: reason=INIT_DATA_MISSING");
            return Optional.empty();
        }
        
        try {
            Map<String, String> parameters = parseQueryString(rawInitData);
            
            String receivedHash = parameters.get("hash");
            String userJson = parameters.get("user");
            String authDateValue = parameters.get("auth_date");
            
            if (receivedHash == null || receivedHash.isBlank()) {
                LOGGER.warn("Telegram initData validation failed: reason=HASH_MISSING");
                return Optional.empty();
            }
            
            if (userJson == null || userJson.isBlank()) {
                LOGGER.warn("Telegram initData validation failed: reason=USER_MISSING");
                return Optional.empty();
            }
            
            if (authDateValue == null || authDateValue.isBlank()) {
                LOGGER.warn("Telegram initData validation failed: reason=AUTH_DATE_MISSING");
                return Optional.empty();
            }
            
            if (!isValidHash(parameters, receivedHash)) {
                LOGGER.warn("Telegram initData validation failed: reason=HASH_MISMATCH");
                return Optional.empty();
            }
            
            Instant authDate;
            
            try {
                authDate = Instant.ofEpochSecond(Long.parseLong(authDateValue));
            } catch (NumberFormatException exception) {
                LOGGER.warnf("Telegram initData validation failed: reason=AUTH_DATE_INVALID, valueLength=%d", authDateValue.length());
                return Optional.empty();
            }
            
            if (!isFresh(authDate)) {
                Instant now = clock.instant();
                long ageSeconds = Duration.between(authDate, now).getSeconds();
                
                LOGGER.warnf("Telegram initData validation failed: reason=AUTH_DATE_EXPIRED_OR_FUTURE, ageSeconds=%d, maximumAgeSeconds=%d", ageSeconds, maximumAge.getSeconds());
                
                return Optional.empty();
            }
            
            TelegramUser user;
            
            try {
                user = objectMapper.readValue(userJson, TelegramUser.class);
            } catch (JsonProcessingException exception) {
                LOGGER.warnf(
                        exception,
                        "Telegram initData validation failed: reason=USER_JSON_INVALID, jsonLength=%d",
                        userJson.length()
                );
                
                return Optional.empty();
            }
            
            if (user.id() <= 0) {
                LOGGER.warnf(
                        "Telegram initData validation failed: reason=USER_ID_INVALID, telegramUserId=%d",
                        user.id()
                );
                
                return Optional.empty();
            }
            
            LOGGER.debugf(
                    "Telegram initData validation succeeded: telegramUserId=%d, authDate=%s",
                    user.id(),
                    authDate
            );
            
            return Optional.of(new TelegramInitData(
                    user,
                    authDate,
                    parameters.get("query_id"),
                    receivedHash,
                    Collections.unmodifiableMap(parameters)
            ));
            
        } catch (GeneralSecurityException exception) {
            LOGGER.error(
                    "Telegram initData validation failed: reason=CRYPTOGRAPHIC_ERROR",
                    exception
            );
            
            return Optional.empty();
        } catch (IllegalArgumentException exception) {
            LOGGER.warnf(
                    exception,
                    "Telegram initData validation failed: reason=QUERY_STRING_INVALID, message=%s",
                    exception.getMessage()
            );
            
            return Optional.empty();
        } catch (Exception exception) {
            LOGGER.error(
                    "Telegram initData validation failed: reason=UNEXPECTED_ERROR",
                    exception
            );
            
            return Optional.empty();
        }
    }
    
    private boolean isFresh(Instant authDate) {
        Instant now = clock.instant();
        
        if (authDate.isAfter(now.plusSeconds(30))) {
            return false;
        }
        
        Duration age = Duration.between(authDate, now);
        return !age.isNegative() && age.compareTo(maximumAge) <= 0;
    }
    
    private boolean isValidHash(Map<String, String> parameters, String receivedHash) throws GeneralSecurityException {
        String dataCheckString = new TreeMap<>(parameters).entrySet().stream().filter(entry -> !"hash".equals(entry.getKey())).map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("\n"));
        
        byte[] secretKey = hmac(WEB_APP_DATA, botToken.getBytes(StandardCharsets.UTF_8));
        byte[] calculatedHash = hmac(secretKey, dataCheckString.getBytes(StandardCharsets.UTF_8));
        byte[] receivedHashBytes;
        
        try {
            receivedHashBytes = HexFormat.of().parseHex(receivedHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
        
        return MessageDigest.isEqual(calculatedHash, receivedHashBytes);
    }
    
    private static byte[] hmac(byte[] key, byte[] value) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_SHA_256);
        mac.init(new SecretKeySpec(key, HMAC_SHA_256));
        
        return mac.doFinal(value);
    }
    
    private static Map<String, String> parseQueryString(String query) {
        Map<String, String> result = new LinkedHashMap<>();
        
        for (String pair : query.split("&")) {
            if (pair.isBlank()) {
                continue;
            }
            
            int separatorIndex = pair.indexOf('=');
            String rawKey = separatorIndex >= 0 ? pair.substring(0, separatorIndex) : pair;
            String rawValue = separatorIndex >= 0 ? pair.substring(separatorIndex + 1) : "";
            String key = URLDecoder.decode(rawKey, StandardCharsets.UTF_8);
            String value = URLDecoder.decode(rawValue, StandardCharsets.UTF_8);
            
            if (result.putIfAbsent(key, value) != null) {
                throw new IllegalArgumentException("Duplicate Telegram initData parameter: " + key);
            }
        }
        
        return result;
    }
}