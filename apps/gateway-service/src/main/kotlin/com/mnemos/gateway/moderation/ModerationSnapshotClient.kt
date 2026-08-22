package com.mnemos.gateway.moderation

import com.mnemos.gateway.configuration.ModerationClientProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.util.retry.Retry
import java.time.Duration

@Component
class ModerationSnapshotClient(
    builder: WebClient.Builder,
    private val properties: ModerationClientProperties,
) {
    private val logger = LoggerFactory.getLogger(ModerationSnapshotClient::class.java)
    private val client = builder.baseUrl(properties.url).build()

    fun load(): List<ActiveBanResponse> =
        client
            .get()
            .uri("/internal/v1/moderation/active-bans")
            .header("X-Mnemos-Service", "gateway-service")
            .header("X-Mnemos-API-Key", properties.apiKey)
            .retrieve()
            .bodyToFlux(ActiveBanResponse::class.java)
            .collectList()
            .retryWhen(snapshotRetryPolicy())
            .block()
            ?: emptyList()

    private fun snapshotRetryPolicy(): Retry =
        Retry
            .backoff(SNAPSHOT_RETRY_COUNT, INITIAL_RETRY_BACKOFF)
            .maxBackoff(MAX_RETRY_BACKOFF)
            .doBeforeRetry { retrySignal ->
                logger.warn(
                    "Failed to load moderation snapshot. Retry {}/{}",
                    retrySignal.totalRetries() + 1,
                    SNAPSHOT_RETRY_COUNT,
                    retrySignal.failure(),
                )
            }

    private companion object {
        const val SNAPSHOT_RETRY_COUNT = 10L

        val INITIAL_RETRY_BACKOFF: Duration = Duration.ofSeconds(1)
        val MAX_RETRY_BACKOFF: Duration = Duration.ofSeconds(5)
    }
}
