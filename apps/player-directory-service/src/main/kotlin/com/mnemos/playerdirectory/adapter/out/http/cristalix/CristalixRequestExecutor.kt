package com.mnemos.playerdirectory.adapter.out.http.cristalix

import com.mnemos.playerdirectory.application.player.exception.PlayerDirectoryUpstreamUnavailableException
import feign.FeignException
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicReference

@Component
class CristalixRequestExecutor(
    properties: CristalixApiProperties,
    private val meterRegistry: MeterRegistry,
    private val clock: Clock,
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
        val state = state(lane)

        if (isCoolingDown(state)) {
            recordCooldownSkip(lane)
            return executeReserve(primaryLane = lane, request = request)
        }

        return try {
            executeWithState(
                lane = lane,
                state = state,
                request = request,
            )
        } catch (exception: FeignException.TooManyRequests) {
            applyUpstreamCooldown(
                lane = lane,
                state = state,
                exception = exception,
            )
            executeReserve(primaryLane = lane, request = request)
        }
    }

    private fun <T> executeReserve(
        primaryLane: CristalixCredentialLane,
        request: (CristalixApiProperties.Credential) -> T,
    ): T {
        if (primaryLane == CristalixCredentialLane.RESERVE) {
            throw PlayerDirectoryUpstreamUnavailableException(
                message = "Cristalix reserve credential is temporarily unavailable",
            )
        }

        val reserve = state(CristalixCredentialLane.RESERVE)

        if (isCoolingDown(reserve)) {
            recordCooldownSkip(CristalixCredentialLane.RESERVE)
            throw PlayerDirectoryUpstreamUnavailableException(
                message = "Cristalix primary and reserve credentials are temporarily unavailable",
            )
        }

        meterRegistry
            .counter(
                "mnemos.player_directory.cristalix.reserve_failover",
                "primary_lane",
                primaryLane.metricValue,
            ).increment()

        return try {
            executeWithState(
                lane = CristalixCredentialLane.RESERVE,
                state = reserve,
                request = request,
            )
        } catch (exception: FeignException.TooManyRequests) {
            applyUpstreamCooldown(
                lane = CristalixCredentialLane.RESERVE,
                state = reserve,
                exception = exception,
            )
            throw PlayerDirectoryUpstreamUnavailableException(
                message = "Cristalix primary and reserve credentials are rate limited",
                cause = exception,
            )
        }
    }

    private fun <T> executeWithState(
        lane: CristalixCredentialLane,
        state: LaneState,
        request: (CristalixApiProperties.Credential) -> T,
    ): T {
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

    private fun applyUpstreamCooldown(
        lane: CristalixCredentialLane,
        state: LaneState,
        exception: FeignException.TooManyRequests,
    ) {
        val now = clock.instant()
        val requestedUntil = retryAfter(exception, now) ?: now.plus(DEFAULT_UPSTREAM_COOLDOWN)
        val cooldownUntil = if (requestedUntil.isAfter(now)) requestedUntil else now.plus(DEFAULT_UPSTREAM_COOLDOWN)

        state.cooldownUntil.updateAndGet { current ->
            if (current == null || cooldownUntil.isAfter(current)) cooldownUntil else current
        }

        meterRegistry
            .counter(
                "mnemos.player_directory.cristalix.cooldowns",
                "lane",
                lane.metricValue,
            ).increment()
    }

    private fun retryAfter(
        exception: FeignException.TooManyRequests,
        now: Instant,
    ): Instant? {
        val value =
            exception
                .responseHeaders()
                .entries
                .firstOrNull { (name, _) -> name.equals(RETRY_AFTER_HEADER, ignoreCase = true) }
                ?.value
                ?.firstOrNull()
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: return null

        value.toLongOrNull()?.let { seconds ->
            if (seconds >= 0) {
                return now.plusSeconds(seconds)
            }
        }

        return runCatching {
            ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()
        }.getOrNull()
    }

    private fun isCoolingDown(state: LaneState): Boolean {
        val until = state.cooldownUntil.get() ?: return false
        val now = clock.instant()

        if (until.isAfter(now)) {
            return true
        }

        state.cooldownUntil.compareAndSet(until, null)
        return false
    }

    private fun recordCooldownSkip(lane: CristalixCredentialLane) {
        meterRegistry
            .counter(
                "mnemos.player_directory.cristalix.cooldown_skips",
                "lane",
                lane.metricValue,
            ).increment()
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

    private fun state(lane: CristalixCredentialLane): LaneState =
        requireNotNull(laneStates[lane]) {
            "Cristalix credential lane $lane is not configured"
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
        val cooldownUntil: AtomicReference<Instant?> = AtomicReference(null),
    )

    private val CristalixCredentialLane.metricValue: String
        get() = name.lowercase()

    private companion object {
        const val RETRY_AFTER_HEADER = "Retry-After"
        val DEFAULT_UPSTREAM_COOLDOWN: Duration = Duration.ofMinutes(1)
    }
}
