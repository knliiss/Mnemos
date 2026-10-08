package com.mnemos.botmanagement

import tools.jackson.databind.JsonNode
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/bot-agents")
@PreAuthorize("authentication.name == 'realtime-service'")
class BotAgentInternalController(private val registry: BotAgentRegistry) {
    @PostMapping("/authenticate")
    fun authenticate(@RequestBody data: JsonNode): Map<String, String> {
        val id = registry.authenticate(data.path("authorization").asText(""))
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        return mapOf("agentId" to id.toString())
    }

    @PostMapping("/{id}/hello")
    fun hello(@PathVariable id: UUID, @RequestBody data: JsonNode): Map<String, Boolean> {
        registry.hello(id, data)
        return mapOf("ok" to true)
    }

    @PostMapping("/{id}/heartbeat")
    fun heartbeat(@PathVariable id: UUID): Map<String, Boolean> {
        registry.heartbeat(id)
        return mapOf("ok" to true)
    }

    @PostMapping("/{id}/disconnected")
    fun disconnected(@PathVariable id: UUID): Map<String, Boolean> {
        registry.disconnected(id)
        return mapOf("ok" to true)
    }
}
