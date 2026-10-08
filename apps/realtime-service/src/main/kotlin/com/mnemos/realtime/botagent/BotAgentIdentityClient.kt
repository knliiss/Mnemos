package com.mnemos.realtime.botagent

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import tools.jackson.databind.JsonNode
import java.time.Duration
import java.util.UUID

/** The bot-management-service is the sole owner of worker identity and persistence. */
@Component
class BotAgentIdentityClient(
    @Value("\${mnemos.bot.management-url}") managementUrl: String,
    @Value("\${mnemos.bot.management-api-key}") private val internalKey: String,
) {
    private val client =
        RestClient
            .builder()
            .baseUrl(managementUrl)
            .requestFactory(
                JdkClientHttpRequestFactory().apply {
                    setReadTimeout(Duration.ofSeconds(5))
                },
            ).build()

    fun authenticate(authorization: String?): UUID? {
        if (authorization == null || !authorization.startsWith("BotAgent ")) return null
        return try {
            val response =
                client
                    .post()
                    .uri("/internal/v1/bot-agents/authenticate")
                    .header("X-Mnemos-Service", "realtime-service")
                    .header("X-Mnemos-API-Key", internalKey)
                    .body(mapOf("authorization" to authorization))
                    .retrieve()
                    .body(JsonNode::class.java)
            response?.path("agentId")?.asText()?.let(UUID::fromString)
        } catch (_: Exception) {
            null
        }
    }

    fun hello(
        id: UUID,
        details: JsonNode,
    ) {
        client
            .post()
            .uri("/internal/v1/bot-agents/{id}/hello", id)
            .header("X-Mnemos-Service", "realtime-service")
            .header("X-Mnemos-API-Key", internalKey)
            .body(details)
            .retrieve()
            .toBodilessEntity()
    }

    fun heartbeat(id: UUID) {
        client
            .post()
            .uri("/internal/v1/bot-agents/{id}/heartbeat", id)
            .header("X-Mnemos-Service", "realtime-service")
            .header("X-Mnemos-API-Key", internalKey)
            .retrieve()
            .toBodilessEntity()
    }

    fun disconnected(id: UUID) {
        runCatching {
            client
                .post()
                .uri("/internal/v1/bot-agents/{id}/disconnected", id)
                .header("X-Mnemos-Service", "realtime-service")
                .header("X-Mnemos-API-Key", internalKey)
                .retrieve()
                .toBodilessEntity()
        }
    }
}
