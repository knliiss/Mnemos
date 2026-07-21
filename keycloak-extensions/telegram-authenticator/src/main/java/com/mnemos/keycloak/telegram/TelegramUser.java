package com.mnemos.keycloak.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramUser(
        long id,
        
        @JsonProperty("first_name")
        String firstName,
        
        @JsonProperty("last_name")
        String lastName,
        
        String username,
        
        @JsonProperty("language_code")
        String languageCode,
        
        @JsonProperty("is_premium")
        Boolean premium,
        
        @JsonProperty("photo_url")
        String photoUrl
) {
}