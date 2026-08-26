package com.mnemos.playerdirectory.adapter.out.http.cristalix

import com.mnemos.playerdirectory.application.player.exception.PlayerDirectoryUpstreamUnavailableException
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import org.springframework.stereotype.Component

@Component
class CristalixRequestExecutor(
    properties: CristalixApiProperties,
) {
    private val laneStates =
        mapOf(
            CristalixCredentialLane.INTERACTIVE to createLaneState("interactive", properties.credentials.interactive),
            CristalixCredentialLane.MONITORING to createLaneState("monitoring", properties.credentials.monitoring),
            CristalixCredentialLane.RESERVE to createLaneState("reserve", properties.credentials.reserve),
        )

    fun <T> execute(
        lane: CristalixCredentialLane,
        request: (CristalixApiProperties.Credential) -> T,
    ): T {
        val state = requireNotNull(laneStates[lane]) { "Cristalix credential lane $lane is not configured" }

        try {
            return state.rateLimiter.executeSupplier {
                request(state.credential)
            }
        } catch (exception: RequestNotPermitted) {
            throw PlayerDirectoryUpstreamUnavailableException(
                message = "Cristalix API quota is temporarily unavailable for $lane requests",
                cause = exception,
            )
        }
    }

    private fun createLaneState(
        name: String,
        credential: CristalixApiProperties.Credential,
    ): LaneState {
        val rateLimit = credential.rateLimit
        val config =
            RateLimiterConfig
                .custom()
                .limitForPeriod(rateLimit.permits)
                .limitRefreshPeriod(rateLimit.refreshPeriod)
                .timeoutDuration(rateLimit.timeout)
                .build()

        return LaneState(
            credential = credential,
            rateLimiter = RateLimiter.of("cristalix-$name", config),
        )
    }

    private data class LaneState(
        val credential: CristalixApiProperties.Credential,
        val rateLimiter: RateLimiter,
    )
}
