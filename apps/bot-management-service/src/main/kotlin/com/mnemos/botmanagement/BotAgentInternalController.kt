package com.mnemos.botmanagement

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.util.UUID

internal const val BOT_INTERNAL_HEADER = "X-Mnemos-Bot-Internal-Token"

/**
 * This is not a browser credential or a worker pairing key. It is supplied to
 * both services by deployment secrets, and is never forwarded to the client.
 */
@Component
class BotInternalAccess(
    @Value("\${mnemos.bot.internal-token}") private val key: String,
) {
    init {
        require(key.length >= 32) { "A dedicated bot-service internal secret (>=32 chars) is required" }
    }

    fun check(supplied: String?) {
        val actual = supplied?.toByteArray(Charsets.UTF_8) ?: byteArrayOf()
        if (!MessageDigest.isEqual(key.toByteArray(Charsets.UTF_8), actual)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
    }
}

@RestController
@RequestMapping("/internal/v1/bot-agents")
class BotAgentInternalController(
    private val registry: BotAgentRegistry,
    private val access: BotInternalAccess,
) {
    @PostMapping("/authenticate")
    fun authenticate(
        @RequestHeader(value = BOT_INTERNAL_HEADER, required = false) secret: String?,
        @RequestBody data: JsonNode,
    ): Map<String, String> {
        access.check(secret)
        val id = registry.authenticate(data.path("authorization").asText(""))
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        return mapOf("agentId" to id.toString())
    }

    @PostMapping("/{id}/hello")
    fun hello(
        @RequestHeader(value = BOT_INTERNAL_HEADER, required = false) secret: String?,
        @PathVariable id: UUID,
        @RequestBody data: JsonNode,
    ): Map<String, Boolean> {
        access.check(secret)
        registry.hello(id, data)
        return mapOf("ok" to true)
    }

    @PostMapping("/{id}/heartbeat")
    fun heartbeat(
        @RequestHeader(value = BOT_INTERNAL_HEADER, required = false) secret: String?,
        @PathVariable id: UUID,
    ): Map<String, Boolean> {
        access.check(secret)
        registry.heartbeat(id)
        return mapOf("ok" to true)
    }

    @PostMapping("/{id}/disconnected")
    fun disconnected(
        @RequestHeader(value = BOT_INTERNAL_HEADER, required = false) secret: String?,
        @PathVariable id: UUID,
    ): Map<String, Boolean> {
        access.check(secret)
        registry.disconnected(id)
        return mapOf("ok" to true)
    }
}
