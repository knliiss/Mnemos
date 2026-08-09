package com.mnemos.account.integration.cristalix.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "mnemos.cristalix-api")
class CristalixApiProperties(
    val url: String,
    val projectToken: String,
    val projectKey: String,
    val rateLimit: RateLimit,
) {
    data class RateLimit(
        val permits: Int,
        val refreshPeriod: Duration,
        val timeout: Duration,
    )
}
