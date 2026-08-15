package com.mnemos.realtime.adapter.inbound.websocket.collector.authentication

import com.mnemos.realtime.application.collector.security.AuthenticateCollectorService
import com.mnemos.realtime.application.collector.security.CollectorAccessKeyParser
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class CollectorHandshakeInterceptor(
    private val accessKeyParser: CollectorAccessKeyParser,
    private val authenticateCollectorService: AuthenticateCollectorService,
) : HandshakeInterceptor {
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        val authorization = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        val encodedKey =
            authorization
                ?.takeIf { it.startsWith(AUTHORIZATION_PREFIX) }
                ?.removePrefix(AUTHORIZATION_PREFIX)
        val accessKey = encodedKey?.let(accessKeyParser::parse)
        val collectorId = accessKey?.let(authenticateCollectorService::authenticate)

        if (collectorId == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED)
            return false
        }

        attributes[COLLECTOR_ID_ATTRIBUTE] = collectorId.id
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?,
    ) = Unit

    companion object {
        const val COLLECTOR_ID_ATTRIBUTE = "mnemosCollectorId"

        private const val AUTHORIZATION_PREFIX = "Collector "
    }
}
