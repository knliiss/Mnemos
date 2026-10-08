package com.mnemos.realtime.botagent

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class BotAgentSessions(
    private val mapper: ObjectMapper,
    private val identity: BotAgentIdentityClient,
) {
    private val active = ConcurrentHashMap<UUID, WebSocketSession>()
    private val lastActive = ConcurrentHashMap<UUID, Instant>()
    private val pending = ConcurrentHashMap<UUID, PendingRequest>()

    private data class PendingRequest(
        val agentId: UUID,
        val sessionId: String,
        val future: CompletableFuture<JsonNode>,
    )

    fun connected(
        id: UUID,
        session: WebSocketSession,
    ) {
        lastActive.remove(id)
        active.put(id, session)?.let { previous ->
            if (previous !== session) {
                runCatching { previous.close(CloseStatus.NORMAL) }
            }
        }
    }

    fun ready(
        id: UUID,
        session: WebSocketSession,
    ) {
        if (active[id] === session) lastActive[id] = Instant.now()
    }

    fun isReady(
        id: UUID,
        session: WebSocketSession,
    ): Boolean = active[id] === session && isOnline(id)

    fun onlineIds(): List<String> =
        active.keys
            .filter(::isOnline)
            .map(UUID::toString)

    fun isOnline(id: UUID): Boolean {
        val lastSeen = lastActive[id] ?: return false
        return active[id]?.isOpen == true && lastSeen.isAfter(Instant.now().minusSeconds(35))
    }

    fun disconnected(
        id: UUID,
        session: WebSocketSession,
    ) {
        if (active.remove(id, session)) {
            lastActive.remove(id)
            pending.values
                .filter { it.agentId == id }
                .forEach { it.future.completeExceptionally(IllegalStateException("Disconnected")) }
            identity.disconnected(id)
        }
    }

    fun close(id: UUID) {
        active.remove(id)?.let {
            runCatching { it.close(CloseStatus.POLICY_VIOLATION) }
        }
        lastActive.remove(id)
        pending.values
            .filter { it.agentId == id }
            .forEach { it.future.completeExceptionally(IllegalStateException("Revoked")) }
        identity.disconnected(id)
    }

    fun complete(
        id: UUID,
        session: WebSocketSession,
        message: JsonNode,
    ) {
        val requestId =
            runCatching { UUID.fromString(message.path("requestId").asText()) }
                .getOrNull()
                ?: return
        val request = pending[requestId] ?: return
        if (request.agentId != id || request.sessionId != session.id || active[id] !== session) return
        if (pending.remove(requestId, request)) request.future.complete(message)
    }

    fun request(
        id: UUID,
        method: String,
        path: String,
        body: JsonNode?,
    ): JsonNode {
        if (!allowed(method, path) || (body != null && body.toString().length > 32_768)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported worker operation")
        }
        val session =
            active[id]?.takeIf { isOnline(id) }
                ?: throw ResponseStatusException(HttpStatus.CONFLICT, "Worker is offline")
        if (pending.size >= 100) throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS)

        val requestId = UUID.randomUUID()
        val response = CompletableFuture<JsonNode>()
        pending[requestId] = PendingRequest(id, session.id, response)

        try {
            send(
                session,
                mapOf(
                    "type" to "request",
                    "requestId" to requestId.toString(),
                    "method" to method,
                    "path" to path,
                    "body" to body,
                ),
            )
            return try {
                val timeout =
                    if (path == "/api/accounts/manage") {
                        75L
                    } else if (path == "/api/proxy-pools" && method == "POST") {
                        65L
                    } else {
                        22L
                    }
                response.get(timeout, TimeUnit.SECONDS)
            } catch (_: java.util.concurrent.TimeoutException) {
                throw ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Worker did not respond")
            } catch (_: Exception) {
                throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Worker request failed")
            }
        } finally {
            pending.remove(requestId)
        }
    }

    fun send(
        session: WebSocketSession,
        value: Any,
    ) {
        synchronized(session) {
            if (session.isOpen) {
                session.sendMessage(TextMessage(mapper.writeValueAsString(value)))
            }
        }
    }

    internal fun allowed(
        method: String,
        path: String,
    ): Boolean {
        if (path.length > 256 || path.contains("..") || path.contains("%") || path.contains("#")) {
            return false
        }
        val noQuery = path.substringBefore('?')
        if (noQuery != path) {
            val permittedQuery =
                method == "GET" &&
                    noQuery.matches(Regex("/api/account/[0-9a-fA-F-]{36}/(logs|payloads)")) &&
                    path.substringAfter('?').matches(Regex("(after|limit)=[0-9]+(&((after|limit)=[0-9]+))*"))
            if (!permittedQuery) return false
        }

        val idPath =
            Regex("/api/account/[0-9a-fA-F-]{36}/(logs|diagnostics|payloads|settings|network|proxy-test|chat|action)")
        if (method == "GET") {
            return noQuery in setOf("/api/overview", "/api/accounts", "/api/manager", "/api/proxy-pools") ||
                (idPath.matches(noQuery) &&
                    noQuery.substringAfterLast('/') in setOf("logs", "diagnostics", "payloads", "network"))
        }
        if (method != "POST") return false
        return noQuery in setOf("/api/manager", "/api/accounts/manage", "/api/proxy-pools") ||
            (idPath.matches(noQuery) &&
                noQuery.substringAfterLast('/') in setOf("settings", "network", "proxy-test", "chat", "action"))
    }
}

@Component
class BotAgentHandshake(
    private val identity: BotAgentIdentityClient,
) : HandshakeInterceptor {
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        val id = identity.authenticate(request.headers.getFirst(HttpHeaders.AUTHORIZATION))
        if (id == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED)
            return false
        }
        attributes["agentId"] = id
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?,
    ) = Unit
}

@Component
class BotAgentSocket(
    private val identity: BotAgentIdentityClient,
    private val sessions: BotAgentSessions,
    private val mapper: ObjectMapper,
) : TextWebSocketHandler() {
    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.connected(id(session), session)
    }

    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage,
    ) {
        if (message.payload.length > 524_288) {
            session.close(CloseStatus.TOO_BIG_TO_PROCESS)
            return
        }
        val node =
            try {
                mapper.readTree(message.payload)
            } catch (_: Exception) {
                session.close(CloseStatus.BAD_DATA)
                return
            }

        val id = id(session)
        when (node.path("type").asText()) {
            "hello" -> {
                try {
                    identity.hello(id, node)
                    sessions.ready(id, session)
                    sessions.send(session, mapOf("type" to "ready"))
                } catch (_: Exception) {
                    session.close(CloseStatus.BAD_DATA)
                }
            }
            "heartbeat" -> {
                if (sessions.isReady(id, session)) {
                    sessions.ready(id, session)
                    identity.heartbeat(id)
                } else {
                    session.close(CloseStatus.POLICY_VIOLATION)
                }
            }
            "response" -> {
                if (sessions.isReady(id, session)) {
                    sessions.complete(id, session, node)
                } else {
                    session.close(CloseStatus.POLICY_VIOLATION)
                }
            }
            else -> session.close(CloseStatus.BAD_DATA)
        }
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        sessions.disconnected(id(session), session)
    }

    override fun handleTransportError(
        session: WebSocketSession,
        exception: Throwable,
    ) {
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

/**
 * Internal relay ONLY: the public gateway has no route to this namespace.
 * Admin requests and durable worker state are owned by bot-management-service.
 */
@RestController
@RequestMapping("/internal/v1/bot-agents")
@PreAuthorize("authentication.name == 'bot-management-service'")
class BotAgentTransportInternalController(
    private val sessions: BotAgentSessions,
) {
    @GetMapping("/online")
    fun online(): List<String> = sessions.onlineIds()

    @PostMapping("/{id}/disconnect")
    fun disconnect(
        @PathVariable id: UUID,
    ): Map<String, Boolean> {
        sessions.close(id)
        return mapOf("ok" to true)
    }

    @PostMapping("/{id}/request")
    fun request(
        @PathVariable id: UUID,
        @RequestBody body: JsonNode,
    ): JsonNode =
        sessions.request(
            id,
            body.path("method").asText(""),
            body.path("path").asText(""),
            body.get("body"),
        )
}
