package com.mnemos.botmanagement

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

@Component
class BotAgentRegistry(private val jdbc: JdbcTemplate) {
    private val random = SecureRandom()

    fun create(): Map<String, Any?> {
        val id = UUID.randomUUID()
        val bytes = ByteArray(32).also(random::nextBytes)
        val key = bytes.joinToString("") { "%02x".format(it) }
        jdbc.update(
            "INSERT INTO bot_agent_instances (id, name, secret_hash) VALUES (?, ?, ?)",
            id, "New worker", hash(key),
        )
        return mapOf("id" to id, "name" to "New worker", "accessKey" to key)
    }

    fun authenticate(header: String?): UUID? {
        if (header == null || !header.startsWith("BotAgent ")) return null
        val key = header.removePrefix("BotAgent ")
        if (!key.matches(Regex("[0-9a-f]{64}"))) return null
        return jdbc.query(
            "SELECT id FROM bot_agent_instances WHERE secret_hash = ? AND NOT revoked",
            { rs, _ -> rs.getObject("id", UUID::class.java) }, hash(key),
        ).firstOrNull()
    }

    fun list(online: (UUID) -> Boolean): List<Map<String, Any?>> =
        jdbc.query(
            """
            SELECT id, name, os_username, machine_name, fingerprint, first_seen_at,
                   last_seen_at, disconnected_at, revoked
            FROM bot_agent_instances ORDER BY COALESCE(last_seen_at, created_at) DESC
            """.trimIndent(),
        ) { rs, _ ->
            val id = rs.getObject("id", UUID::class.java)
            mapOf(
                "id" to id.toString(), "name" to rs.getString("name"),
                "osUsername" to rs.getString("os_username"),
                "machineName" to rs.getString("machine_name"),
                "fingerprint" to rs.getString("fingerprint"),
                "firstSeenAt" to rs.getTimestamp("first_seen_at")?.toInstant()?.toString(),
                "lastSeenAt" to rs.getTimestamp("last_seen_at")?.toInstant()?.toString(),
                "disconnectedAt" to rs.getTimestamp("disconnected_at")?.toInstant()?.toString(),
                "online" to (online(id) && !rs.getBoolean("revoked")),
                "revoked" to rs.getBoolean("revoked"),
            )
        }

    fun hello(id: UUID, data: JsonNode) {
        val username = data.path("username").asText("").take(100).trim()
        val machine = data.path("machineName").asText("").take(100).trim()
        val fingerprint = data.path("fingerprint").asText("")
        if (username.isBlank() || !fingerprint.matches(Regex("[0-9a-f]{64}"))) {
            throw IllegalArgumentException("Invalid machine identity")
        }
        val changed = jdbc.update(
            """
            UPDATE bot_agent_instances
            SET os_username = ?, machine_name = ?, fingerprint = ?,
                name = CASE WHEN name = 'New worker' THEN ? ELSE name END,
                first_seen_at = COALESCE(first_seen_at, NOW()), last_seen_at = NOW(),
                disconnected_at = NULL
            WHERE id = ? AND NOT revoked AND (fingerprint IS NULL OR fingerprint = ?)
            """.trimIndent(),
            username, machine, fingerprint, username, id, fingerprint,
        )
        if (changed == 0) throw IllegalArgumentException("Worker identity mismatch")
    }

    fun heartbeat(id: UUID) {
        jdbc.update(
            "UPDATE bot_agent_instances SET last_seen_at = NOW() WHERE id = ? AND NOT revoked", id,
        )
    }

    fun disconnected(id: UUID) {
        jdbc.update(
            "UPDATE bot_agent_instances SET disconnected_at = NOW() WHERE id = ?", id,
        )
    }

    fun rename(id: UUID, name: String) {
        val cleaned = name.trim()
        if (cleaned.length !in 1..100) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid name")
        if (jdbc.update("UPDATE bot_agent_instances SET name = ? WHERE id = ? AND NOT revoked", cleaned, id) == 0) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    fun revoke(id: UUID) {
        if (jdbc.update("UPDATE bot_agent_instances SET revoked = TRUE WHERE id = ?", id) == 0) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    private fun hash(key: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(key.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}

