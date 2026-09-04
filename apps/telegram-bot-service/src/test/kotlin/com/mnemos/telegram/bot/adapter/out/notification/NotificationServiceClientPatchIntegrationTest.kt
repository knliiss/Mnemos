package com.mnemos.telegram.bot.adapter.out.notification

import com.fasterxml.jackson.databind.ObjectMapper
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateRaidSubscriptionRequest
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

private const val TEST_NOTIFICATION_API_KEY = "raid-patch-test-api-key"

@SpringBootTest(
    properties = [
        "mnemos.telegram-bot.telegram.enabled=false",
        "mnemos.clients.notification-service.api-key=$TEST_NOTIFICATION_API_KEY",
    ],
)
class NotificationServiceClientPatchIntegrationTest {
    @Autowired
    private lateinit var client: NotificationServiceClient

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `raid update reaches notification service as authenticated PATCH`() {
        requestMethods.clear()
        requestPaths.clear()
        requestBodies.clear()
        requestServiceNames.clear()
        requestApiKeys.clear()

        val response =
            client.updateRaidSubscription(
                telegramId = TELEGRAM_ID,
                request =
                    TelegramCreateRaidSubscriptionRequest(
                        locations = setOf(3, 12),
                    ),
            )

        val body = objectMapper.readTree(requestBodies.poll(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS))
        val locations = body.path("locations").map { node -> node.asInt() }.toSet()

        assertEquals(SUBSCRIPTION_ID, response.subscriptionId)
        assertEquals("PATCH", requestMethods.poll(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS))
        assertEquals(
            "/internal/v1/telegram/subscriptions/$TELEGRAM_ID/raid",
            requestPaths.poll(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS),
        )
        assertEquals(setOf(3, 12), locations)
        assertEquals("telegram-bot-service", requestServiceNames.poll(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS))
        assertEquals(TEST_NOTIFICATION_API_KEY, requestApiKeys.poll(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS))
    }

    private companion object {
        const val TELEGRAM_ID = 931458258L
        const val REQUEST_TIMEOUT_SECONDS = 2L

        val SUBSCRIPTION_ID: UUID = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b75")
        val requestMethods = LinkedBlockingQueue<String>()
        val requestPaths = LinkedBlockingQueue<String>()
        val requestBodies = LinkedBlockingQueue<String>()
        val requestServiceNames = LinkedBlockingQueue<String>()
        val requestApiKeys = LinkedBlockingQueue<String>()

        val server: HttpServer =
            HttpServer
                .create(InetSocketAddress("127.0.0.1", 0), 0)
                .apply {
                    createContext("/internal/v1/telegram/subscriptions/$TELEGRAM_ID/raid") { exchange ->
                        handleRaidUpdate(exchange)
                    }
                    start()
                }

        @JvmStatic
        @DynamicPropertySource
        fun notificationServiceProperties(registry: DynamicPropertyRegistry) {
            registry.add("mnemos.clients.notification-service.url") {
                "http://127.0.0.1:${server.address.port}"
            }
        }

        @JvmStatic
        @AfterAll
        fun stopServer() {
            server.stop(0)
        }

        fun handleRaidUpdate(exchange: HttpExchange) {
            requestMethods.add(exchange.requestMethod)
            requestPaths.add(exchange.requestURI.path)
            requestBodies.add(
                exchange.requestBody
                    .bufferedReader(StandardCharsets.UTF_8)
                    .use { reader -> reader.readText() },
            )
            requestServiceNames.add(exchange.requestHeaders.getFirst("X-Mnemos-Service").orEmpty())
            requestApiKeys.add(exchange.requestHeaders.getFirst("X-Mnemos-API-Key").orEmpty())

            val response = "{\"subscriptionId\":\"$SUBSCRIPTION_ID\"}"
            val responseBytes = response.toByteArray(StandardCharsets.UTF_8)

            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, responseBytes.size.toLong())
            exchange.responseBody.use { output -> output.write(responseBytes) }
        }
    }
}
