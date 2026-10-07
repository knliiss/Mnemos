package com.mnemos.realtime.botagent

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Private, outbound-only C7x worker transport. A worker key is independent of
 * the user-facing Keycloak session and can never authorize the admin REST API.
 */
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
        jdbc.update(
            """
            UPDATE bot_agent_instances
            SET os_username = ?, machine_name = ?, fingerprint = ?,
                name = CASE WHEN name = 'New worker' THEN ? ELSE name END,
                first_seen_at = COALESCE(first_seen_at, NOW()), last_seen_at = NOW(),
                disconnected_at = NULL
            WHERE id = ? AND NOT revoked
            """.trimIndent(),
            username, machine, fingerprint, username, id,
        )
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

@Component
class BotAgentSessions(private val mapper: ObjectMapper, private val registry: BotAgentRegistry) {
    private val active = ConcurrentHashMap<UUID, WebSocketSession>()
    private val pending = ConcurrentHashMap<UUID, CompletableFuture<JsonNode>>()

    fun connected(id: UUID, session: WebSocketSession) {
        active.put(id, session)?.let { previous ->
            if (previous !== session) runCatching { previous.close(CloseStatus.NORMAL) }
        }
    }

    fun isOnline(id: UUID): Boolean = active[id]?.isOpen == true

    fun disconnected(id: UUID, session: WebSocketSession) {
        if (active.remove(id, session)) registry.disconnected(id)
    }

    fun close(id: UUID) {
        active.remove(id)?.let { runCatching { it.close(CloseStatus.POLICY_VIOLATION) } }
        registry.disconnected(id)
    }

    fun complete(message: JsonNode) {
        val requestId = runCatching { UUID.fromString(message.path("requestId").asText()) }.getOrNull() ?: return
        pending.remove(requestId)?.complete(message)
    }

    fun request(id: UUID, method: String, path: String, body: JsonNode?): JsonNode {
        if (!allowed(method, path) || (body != null && body.toString().length > 32_768)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported worker operation")
        }
        val session = active[id]?.takeIf { it.isOpen }
            ?: throw ResponseStatusException(HttpStatus.CONFLICT, "Worker is offline")
        if (pending.size >= 100) throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS)
        val requestId = UUID.randomUUID()
        val response = CompletableFuture<JsonNode>()
        pending[requestId] = response
        try {
            send(session, mapOf("type" to "request", "requestId" to requestId.toString(),
                "method" to method, "path" to path, "body" to body))
            return try {
                response.get(12, TimeUnit.SECONDS)
            } catch (_: java.util.concurrent.TimeoutException) {
                throw ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Worker did not respond")
            } catch (_: Exception) {
                throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Worker request failed")
            }
        } finally {
            pending.remove(requestId)
        }
    }

    fun send(session: WebSocketSession, value: Any) {
        synchronized(session) {
            if (session.isOpen) session.sendMessage(TextMessage(mapper.writeValueAsString(value)))
        }
    }

    private fun allowed(method: String, path: String): Boolean {
        if (path.length > 256 || path.contains("..") || path.contains("%") || path.contains("#")) return false
        val noQuery = path.substringBefore('?')
        if (noQuery != path && !(method == "GET" &&
                    noQuery.matches(Regex("/api/account/[0-9a-fA-F-]{36}/(logs|payloads)")) &&
                    path.substringAfter('?').matches(Regex("(after|limit)=[0-9]+(&((after|limit)=[0-9]+))*")))) return false
        val idPath = Regex("/api/account/[0-9a-fA-F-]{36}/(logs|diagnostics|payloads|settings|network|proxy-test|chat|action)")
        if (method == "GET") {
            return noQuery in setOf("/api/overview", "/api/accounts", "/api/manager", "/api/proxy-pools") ||
                (idPath.matches(noQuery) && noQuery.substringAfterLast('/') in
                    setOf("logs", "diagnostics", "payloads", "network"))
        }
        if (method != "POST") return false
        return noQuery in setOf("/api/manager", "/api/accounts/manage", "/api/proxy-pools") ||
            (idPath.matches(noQuery) && noQuery.substringAfterLast('/') in
                setOf("settings", "network", "proxy-test", "chat", "action"))
    }
}

@Component
class BotAgentHandshake(private val registry: BotAgentRegistry) : HandshakeInterceptor {
    override fun beforeHandshake(
        request: ServerHttpRequest, response: ServerHttpResponse,
        wsHandler: WebSocketHandler, attributes: MutableMap<String, Any>,
    ): Boolean {
        val id = registry.authenticate(request.headers.getFirst(HttpHeaders.AUTHORIZATION))
        if (id == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED)
            return false
        }
        attributes["agentId"] = id
        return true
    }
    override fun afterHandshake(
        request: ServerHttpRequest, response: ServerHttpResponse,
        wsHandler: WebSocketHandler, exception: Exception?,
    ) = Unit
}

@Component
class BotAgentSocket(
    private val registry: BotAgentRegistry,
    private val sessions: BotAgentSessions,
    private val mapper: ObjectMapper,
) : TextWebSocketHandler() {
    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.connected(id(session), session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        if (message.payload.length > 524_288) {
            session.close(CloseStatus.TOO_BIG_TO_PROCESS)
            return
        }
        val node = try { mapper.readTree(message.payload) } catch (_: Exception) {
            session.close(CloseStatus.BAD_DATA)
            return
        }
        val id = id(session)
        when (node.path("type").asText()) {
            "hello" -> try {
                registry.hello(id, node)
                sessions.send(session, mapOf("type" to "ready"))
            } catch (_: IllegalArgumentException) { session.close(CloseStatus.BAD_DATA) }
            "heartbeat" -> registry.heartbeat(id)
            "response" -> sessions.complete(node)
            else -> session.close(CloseStatus.BAD_DATA)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.disconnected(id(session), session)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        sessions.disconnected(id(session), session)
        runCatching { session.close(CloseStatus.SERVER_ERROR) }
    }

    private fun id(session: WebSocketSession): UUID = session.attributes["agentId"] as UUID
}

@Configuration
class BotAgentWebSocketConfiguration(
    private val socket: BotAgentSocket,
    private val handshake: BotAgentHandshake,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(socket, "/ws/v1/bot-agent").addInterceptors(handshake)
    }
}

@RestController
@RequestMapping("/api/v1/realtime/admin/bot-agents")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
class BotAgentAdminController(
    private val registry: BotAgentRegistry,
    private val sessions: BotAgentSessions,
) {
    @GetMapping
    fun list() = registry.list(sessions::isOnline)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create() = registry.create()

    @PostMapping("/{id}/rename")
    fun rename(@PathVariable id: UUID, @RequestBody body: JsonNode): Map<String, Boolean> {
        registry.rename(id, body.path("name").asText(""))
        return mapOf("ok" to true)
    }

    @DeleteMapping("/{id}")
    fun revoke(@PathVariable id: UUID): Map<String, Boolean> {
        registry.revoke(id)
        sessions.close(id)
        return mapOf("ok" to true)
    }

    @PostMapping("/{id}/request")
    fun request(@PathVariable id: UUID, @RequestBody body: JsonNode): JsonNode {
        val method = body.path("method").asText("")
        val path = body.path("path").asText("")
        return sessions.request(id, method, path, body.get("body"))
    }
}
