package com.mnemos.botmanagement

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.util.UUID

/** Single-instance transport endpoint; scale-out requires a worker session router. */
@Component
class BotAgentTransportClient(
    @Value("\${mnemos.bot.realtime-url}") baseUrl: String,
    @Value("\${mnemos.bot.internal-token}") private val key: String,
) {
    private val http = RestClient.builder().baseUrl(baseUrl)
        .requestFactory(
            org.springframework.http.client.JdkClientHttpRequestFactory().apply {
                setReadTimeout(Duration.ofSeconds(85))
            },
        )
        .build()

    fun onlineIds(): Set<UUID> = try {
        http.get().uri("/internal/v1/bot-agents/online")
            .header(BOT_INTERNAL_HEADER, key)
            .retrieve().body(Array<String>::class.java)
            ?.mapNotNull { runCatching { UUID.fromString(it) }.getOrNull() }?.toSet() ?: emptySet()
    } catch (_: Exception) {
        emptySet()
    }

    fun disconnect(id: UUID) {
        try {
            http.post().uri("/internal/v1/bot-agents/{id}/disconnect", id)
                .header(BOT_INTERNAL_HEADER, key)
                .retrieve().toBodilessEntity()
        } catch (_: Exception) {
            // The database revocation takes effect even if the worker is already offline.
        }
    }

    fun request(id: UUID, body: JsonNode): JsonNode {
        try {
            return http.post().uri("/internal/v1/bot-agents/{id}/request", id)
                .header(BOT_INTERNAL_HEADER, key)
                .body(body)
                .retrieve().body(JsonNode::class.java)
                ?: throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty worker reply")
        } catch (error: RestClientResponseException) {
            throw ResponseStatusException(
                HttpStatus.valueOf(error.statusCode.value()),
                "Bot transport rejected request",
            )
        }
    }
}
