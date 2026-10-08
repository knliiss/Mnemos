package com.mnemos.realtime.configuration.websocket

import com.mnemos.realtime.adapter.inbound.websocket.collector.authentication.CollectorHandshakeInterceptor
import com.mnemos.realtime.adapter.inbound.websocket.collector.ingress.CollectorWebSocketHandler
import com.mnemos.realtime.configuration.collector.CollectorConnectionProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean

@Configuration
@EnableWebSocket
class CollectorWebSocketConfiguration(
    private val handler: CollectorWebSocketHandler,
    private val handshakeInterceptor: CollectorHandshakeInterceptor,
    private val properties: CollectorConnectionProperties,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(
                handler,
                "/ws/v1/collector",
            ).addInterceptors(handshakeInterceptor)
    }

    @Bean
    fun collectorWebSocketContainer(): ServletServerContainerFactoryBean =
        ServletServerContainerFactoryBean().apply {
            setMaxTextMessageBufferSize(524_288) // Bot-agent diagnostic responses can be larger than collector reports.
            setMaxBinaryMessageBufferSize(BINARY_MESSAGE_BUFFER_BYTES)
            setMaxSessionIdleTimeout(properties.heartbeatTimeout.toMillis())
        }

    private companion object {
        const val BINARY_MESSAGE_BUFFER_BYTES = 1_024
    }
}
