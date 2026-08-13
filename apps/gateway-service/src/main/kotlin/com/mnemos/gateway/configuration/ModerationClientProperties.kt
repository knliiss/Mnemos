package com.mnemos.gateway.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mnemos.gateway.moderation")
data class ModerationClientProperties(
    val url: String = "http://moderation-service:8087",
    val apiKey: String,
)
