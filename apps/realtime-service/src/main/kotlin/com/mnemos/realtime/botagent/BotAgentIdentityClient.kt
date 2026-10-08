package com.mnemos.realtime.botagent

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.util.UUID

internal const val BOT_INTERNAL_HEADER = "X-Mnemos-Bot-Internal-Token"

/** The bot-management-service is the sole owner of worker identity and persistence. */
@Component
class BotAgentIdentityClient(
    @Value("\${mnemos.bot.management-url}") managementUrl: String,
    @Value("\${mnemos.bot.internal-token}") private val internalKey: String,
) {
    private val client = RestClient.builder().baseUrl(managementUrl)
        .requestFactory(
            org.springframework.http.client.JdkClientHttpRequestFactory().apply {
                setReadTimeout(Duration.ofSeconds(5))
            },
        )
        .build()

    fun authenticate(authorization: String?): UUID? {
        if (authorization == null || !authorization.startsWith("BotAgent ")) return null
        return try {
            val response = client.post().uri("/internal/v1/bot-agents/authenticate")
                .header(BOT_INTERNAL_HEADER, internalKey)
                .body(mapOf("authorization" to authorization))
                .retrieve().body(JsonNode::class.java)
            UUID.fromString(response?.path("agentId")?.asText())
        } catch (_: Exception) {
            null
        }
    }

    fun hello(id: UUID, details: JsonNode) {
        client.post().uri("/internal/v1/bot-agents/{id}/hello", id)
            .header(BOT_INTERNAL_HEADER, internalKey)
            .body(details)
            .retrieve().toBodilessEntity()
    }

    fun heartbeat(id: UUID) {
        client.post().uri("/internal/v1/bot-agents/{id}/heartbeat", id)
            .header(BOT_INTERNAL_HEADER, internalKey)
            .retrieve().toBodilessEntity()
    }

    fun disconnected(id: UUID) {
        runCatching {
            client.post().uri("/internal/v1/bot-agents/{id}/disconnected", id)
                .header(BOT_INTERNAL_HEADER, internalKey)
                .retrieve().toBodilessEntity()
        }
    }
}
