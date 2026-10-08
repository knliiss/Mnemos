package com.mnemos.botmanagement

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.JsonNode
import java.time.Duration
import java.util.UUID

/** Single-instance transport endpoint; scale-out requires a worker session router. */
@Component
class BotAgentTransportClient(
    @Value("\${mnemos.bot.realtime-url}") baseUrl: String,
    @Value("\${mnemos.bot.realtime-api-key}") private val key: String,
) {
    private val http =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestFactory(
                JdkClientHttpRequestFactory().apply {
                    setReadTimeout(Duration.ofSeconds(85))
                },
            ).build()

    fun onlineIds(): Set<UUID> =
        try {
            val online =
                http
                    .get()
                    .uri("/internal/v1/bot-agents/online")
                    .header("X-Mnemos-Service", "bot-management-service")
                    .header("X-Mnemos-API-Key", key)
                    .retrieve()
                    .body(Array<String>::class.java)
            online
                ?.mapNotNull { runCatching { UUID.fromString(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet()
        } catch (_: Exception) {
            emptySet()
        }

    fun disconnect(id: UUID) {
        try {
            http
                .post()
                .uri("/internal/v1/bot-agents/{id}/disconnect", id)
                .header("X-Mnemos-Service", "bot-management-service")
                .header("X-Mnemos-API-Key", key)
                .retrieve()
                .toBodilessEntity()
        } catch (_: Exception) {
            // Database revocation applies even if the worker is already offline.
        }
    }

    fun request(
        id: UUID,
        body: JsonNode,
    ): JsonNode {
        try {
            return http
                .post()
                .uri("/internal/v1/bot-agents/{id}/request", id)
                .header("X-Mnemos-Service", "bot-management-service")
                .header("X-Mnemos-API-Key", key)
                .body(body)
                .retrieve()
                .body(JsonNode::class.java)
                ?: throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty worker reply")
        } catch (error: RestClientResponseException) {
            throw ResponseStatusException(
                HttpStatus.valueOf(error.statusCode.value()),
                "Bot transport rejected request",
            )
        }
    }
}
