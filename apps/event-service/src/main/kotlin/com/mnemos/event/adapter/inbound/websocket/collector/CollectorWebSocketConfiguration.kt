package com.mnemos.event.adapter.inbound.websocket.collector

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class CollectorWebSocketConfiguration(
    private val collectorWebSocketHandler: CollectorWebSocketHandler,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(
            collectorWebSocketHandler,
            "/ws/v1/collector",
        )
    }
}
