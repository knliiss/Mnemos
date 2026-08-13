package com.mnemos.gateway.moderation

import com.mnemos.gateway.configuration.ModerationClientProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class ModerationSnapshotClient(
    builder: WebClient.Builder,
    private val properties: ModerationClientProperties,
) {
    private val client = builder.baseUrl(properties.url).build()

    fun load(): List<ActiveBanResponse> =
        client
            .get()
            .uri("/internal/v1/moderation/active-bans")
            .header("X-Mnemos-Service", "gateway-service")
            .header("X-Mnemos-API-Key", properties.apiKey)
            .retrieve()
            .bodyToFlux(ActiveBanResponse::class.java)
            .collectList()
            .block()
            ?: emptyList()
}
