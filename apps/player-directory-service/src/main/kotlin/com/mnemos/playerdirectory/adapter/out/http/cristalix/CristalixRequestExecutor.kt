package com.mnemos.playerdirectory.adapter.out.http.cristalix

import com.mnemos.playerdirectory.application.player.exception.PlayerDirectoryUpstreamUnavailableException
import feign.FeignException
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component

@Component
class CristalixRequestExecutor(
    properties: CristalixApiProperties,
    private val meterRegistry: MeterRegistry,
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
        val sample = Timer.start(meterRegistry)

        try {
            val result =
                state.rateLimiter.executeSupplier {
                    request(state.credential)
                }

            recordRequest(lane = lane, outcome = "success", sample = sample)
            return result
        } catch (exception: RequestNotPermitted) {
            recordRequest(lane = lane, outcome = "local_quota_rejected", sample = sample)
            meterRegistry
                .counter(
                    "mnemos.player_directory.cristalix.quota_rejections",
                    "lane",
                    lane.metricValue,
                ).increment()

            throw PlayerDirectoryUpstreamUnavailableException(
                message = "Cristalix API quota is temporarily unavailable for $lane requests",
                cause = exception,
            )
        } catch (exception: FeignException.TooManyRequests) {
            recordRequest(lane = lane, outcome = "upstream_429", sample = sample)
            meterRegistry
                .counter(
                    "mnemos.player_directory.cristalix.upstream_429",
                    "lane",
                    lane.metricValue,
                ).increment()
            throw exception
        } catch (exception: Exception) {
            recordRequest(lane = lane, outcome = "failure", sample = sample)
            throw exception
        }
    }

    private fun recordRequest(
        lane: CristalixCredentialLane,
        outcome: String,
        sample: Timer.Sample,
    ) {
        meterRegistry
            .counter(
                "mnemos.player_directory.cristalix.requests",
                "lane",
                lane.metricValue,
                "outcome",
                outcome,
            ).increment()

        sample.stop(
            meterRegistry.timer(
                "mnemos.player_directory.cristalix.request_duration",
                "lane",
                lane.metricValue,
                "outcome",
                outcome,
            ),
        )
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

    private val CristalixCredentialLane.metricValue: String
        get() = name.lowercase()
}
