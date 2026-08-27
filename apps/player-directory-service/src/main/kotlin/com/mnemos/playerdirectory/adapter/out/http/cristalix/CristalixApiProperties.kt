package com.mnemos.playerdirectory.adapter.out.http.cristalix

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "mnemos.player-directory.cristalix-api")
class CristalixApiProperties(
    val url: String,
    val connectTimeout: Duration,
    val readTimeout: Duration,
    val credentials: Credentials,
) {
    data class Credentials(
        val interactive: Credential,
        val monitoring: Credential,
        val reserve: Credential,
    )

    data class Credential(
        val projectKey: String,
        val projectToken: String,
        val rateLimit: RateLimit,
    )

    data class RateLimit(
        val permits: Int,
        val refreshPeriod: Duration,
        val timeout: Duration,
    )
}
