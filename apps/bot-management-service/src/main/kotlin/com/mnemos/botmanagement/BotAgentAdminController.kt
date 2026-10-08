package com.mnemos.botmanagement

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/bot-management/admin/bot-agents")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
class BotAgentAdminController(
    private val registry: BotAgentRegistry,
    private val transport: BotAgentTransportClient,
) {
    @GetMapping
    fun list() = registry.list(transport.onlineIds()::contains)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create() = registry.create()

    @PostMapping("/{id}/rename")
    fun rename(
        @PathVariable id: UUID,
        @RequestBody body: JsonNode,
    ): Map<String, Boolean> {
        registry.rename(id, body.path("name").asText(""))
        return mapOf("ok" to true)
    }

    @DeleteMapping("/{id}")
    fun revoke(@PathVariable id: UUID): Map<String, Boolean> {
        registry.revoke(id)
        transport.disconnect(id)
        return mapOf("ok" to true)
    }

    @PostMapping("/{id}/request")
    fun request(
        @PathVariable id: UUID,
        @RequestBody body: JsonNode,
    ): JsonNode = transport.request(id, body)
}
