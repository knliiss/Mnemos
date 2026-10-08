package com.mnemos.botmanagement

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Repository
class BotAgentGroupStore(
    private val jdbc: JdbcTemplate,
    private val mapper: ObjectMapper,
) {
    fun get(agentId: UUID): Map<String, Any?> {
        checkAgent(agentId)
        return jdbc.query(
            "SELECT payload::text AS payload, revision FROM bot_agent_group_state WHERE agent_id = ?",
            { rs, _ ->
                mapOf("state" to mapper.readTree(rs.getString("payload")), "revision" to rs.getLong("revision"))
            },
            agentId,
        ).firstOrNull() ?: mapOf(
            "state" to mapper.readTree("""{"groups":[],"assignments":{}}"""),
            "revision" to 0L,
        )
    }

    fun save(
        agentId: UUID,
        state: JsonNode,
        expectedRevision: Long,
    ): Map<String, Any?> {
        checkAgent(agentId)
        validate(state)
        val content = mapper.writeValueAsString(state)
        val changed =
            if (expectedRevision == 0L) {
                jdbc.update(
                    """
                    INSERT INTO bot_agent_group_state (agent_id, payload, revision)
                    VALUES (?, CAST(? AS jsonb), 1)
                    ON CONFLICT (agent_id) DO NOTHING
                    """.trimIndent(),
                    agentId, content,
                )
            } else {
                jdbc.update(
                    """
                    UPDATE bot_agent_group_state
                    SET payload = CAST(? AS jsonb), revision = revision + 1, updated_at = NOW()
                    WHERE agent_id = ? AND revision = ?
                    """.trimIndent(),
                    content, agentId, expectedRevision,
                )
            }
        if (changed != 1) throw ResponseStatusException(HttpStatus.CONFLICT, "Groups changed; refresh the list")
        return get(agentId)
    }

    private fun checkAgent(id: UUID) {
        val count = jdbc.queryForObject("SELECT COUNT(*) FROM bot_agent_instances WHERE id = ?", Int::class.java, id)
        if (count != 1) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown worker")
    }

    private fun validate(state: JsonNode) {
        val groups = state.path("groups")
        val assignments = state.path("assignments")
        if (!groups.isArray || groups.size() > 100 || !assignments.isObject || assignments.size() > 5000) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid groups")
        }
        val ids = mutableSetOf<String>()
        groups.forEach { group ->
            val id = group.path("id").asText()
            val name = group.path("name").asText()
            if (!id.matches(Regex("[a-zA-Z0-9_-]{1,64}")) || name.length !in 1..64 || !ids.add(id)) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid group name/id")
            }
        }
        val iterator = assignments.fields()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (runCatching { UUID.fromString(entry.key) }.isFailure ||
                !entry.value.isTextual ||
                !ids.contains(entry.value.asText())
            ) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid account assignment")
            }
        }
    }
}

@RestController
@RequestMapping("/api/v1/bot-management/admin/bot-agents/{agentId}/groups")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
class BotAgentGroupsController(private val store: BotAgentGroupStore) {
    @GetMapping
    fun get(@PathVariable agentId: UUID) = store.get(agentId)

    @PutMapping
    fun save(
        @PathVariable agentId: UUID,
        @RequestBody request: JsonNode,
    ) = store.save(
        agentId, request.path("state"), request.path("revision").asLong(-1),
    )
}
