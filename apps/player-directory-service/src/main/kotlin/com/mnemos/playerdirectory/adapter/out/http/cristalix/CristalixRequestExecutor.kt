package com.mnemos.playerdirectory.adapter.out.http.cristalix

import com.mnemos.playerdirectory.adapter.out.http.cristalix.quota.CristalixDistributedQuotaCoordinator
import com.mnemos.playerdirectory.adapter.out.http.cristalix.quota.CristalixQuotaDecision
import com.mnemos.playerdirectory.adapter.out.http.cristalix.quota.CristalixQuotaDecisionStatus
import com.mnemos.playerdirectory.application.player.exception.PlayerDirectoryUpstreamUnavailableException
import feign.FeignException
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.locks.LockSupport
import kotlin.math.min

@Component
class CristalixRequestExecutor(
    properties: CristalixApiProperties,
    private val quotaCoordinator: CristalixDistributedQuotaCoordinator,
    private val meterRegistry: MeterRegistry,
    private val clock: Clock,
) {
    private val credentials =
        mapOf(
            CristalixCredentialLane.INTERACTIVE to properties.credentials.interactive,
            CristalixCredentialLane.MONITORING to properties.credentials.monitoring,
            CristalixCredentialLane.RESERVE to properties.credentials.reserve,
        )

    fun <T> execute(
        lane: CristalixCredentialLane,
        request: (CristalixApiProperties.Credential) -> T,
    ): T {
        val credential = credential(lane)
        val quotaDecision = awaitQuotaPermit(lane, credential)

        if (quotaDecision.status == CristalixQuotaDecisionStatus.COOLING_DOWN) {
            recordCooldownSkip(lane)
            return executeReserve(primaryLane = lane, request = request)
        }

        if (quotaDecision.status == CristalixQuotaDecisionStatus.QUOTA_EXHAUSTED) {
            recordQuotaRejection(lane)
            throw quotaUnavailable(lane)
        }

        return try {
            executeRequest(
                lane = lane,
                credential = credential,
                request = request,
            )
        } catch (exception: FeignException.TooManyRequests) {
            applyUpstreamCooldown(
                lane = lane,
                credential = credential,
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

        val lane = CristalixCredentialLane.RESERVE
        val credential = credential(lane)
        val quotaDecision = awaitQuotaPermit(lane, credential)

        if (quotaDecision.status == CristalixQuotaDecisionStatus.COOLING_DOWN) {
            recordCooldownSkip(lane)
            throw PlayerDirectoryUpstreamUnavailableException(
                message = "Cristalix primary and reserve credentials are temporarily unavailable",
            )
        }

        if (quotaDecision.status == CristalixQuotaDecisionStatus.QUOTA_EXHAUSTED) {
            recordQuotaRejection(lane)
            throw quotaUnavailable(lane)
        }

        meterRegistry
            .counter(
                "mnemos.player_directory.cristalix.reserve_failover",
                "primary_lane",
                primaryLane.metricValue,
            ).increment()

        return try {
            executeRequest(
                lane = lane,
                credential = credential,
                request = request,
            )
        } catch (exception: FeignException.TooManyRequests) {
            applyUpstreamCooldown(
                lane = lane,
                credential = credential,
                exception = exception,
            )
            throw PlayerDirectoryUpstreamUnavailableException(
                message = "Cristalix primary and reserve credentials are rate limited",
                cause = exception,
            )
        }
    }

    private fun awaitQuotaPermit(
        lane: CristalixCredentialLane,
        credential: CristalixApiProperties.Credential,
    ): CristalixQuotaDecision {
        val timeoutNanos = credential.rateLimit.timeout.toNanos()
        val deadlineNanos = System.nanoTime() + timeoutNanos

        while (true) {
            val decision =
                quotaCoordinator.tryAcquire(
                    lane = lane,
                    rateLimit = credential.rateLimit,
                    now = clock.instant(),
                )

            if (decision.status != CristalixQuotaDecisionStatus.QUOTA_EXHAUSTED) {
                return decision
            }

            val remainingNanos = deadlineNanos - System.nanoTime()

            if (timeoutNanos == 0L || remainingNanos <= 0L) {
                return decision
            }

            val retryAfterNanos = decision.retryAfter?.toNanos() ?: remainingNanos
            val waitNanos = min(remainingNanos, retryAfterNanos)

            if (waitNanos <= 0L) {
                return decision
            }

            LockSupport.parkNanos(waitNanos)

            if (Thread.currentThread().isInterrupted) {
                return decision
            }
        }
    }

    private fun <T> executeRequest(
        lane: CristalixCredentialLane,
        credential: CristalixApiProperties.Credential,
        request: (CristalixApiProperties.Credential) -> T,
    ): T {
        val sample = Timer.start(meterRegistry)

        return try {
            val result = request(credential)

            recordRequest(lane = lane, outcome = "success", sample = sample)
            result
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
        credential: CristalixApiProperties.Credential,
        exception: FeignException.TooManyRequests,
    ) {
        val now = clock.instant()
        val requestedUntil = retryAfter(exception, now) ?: now.plus(DEFAULT_UPSTREAM_COOLDOWN)
        val cooldownUntil = if (requestedUntil.isAfter(now)) requestedUntil else now.plus(DEFAULT_UPSTREAM_COOLDOWN)

        quotaCoordinator.applyCooldown(
            lane = lane,
            rateLimit = credential.rateLimit,
            cooldownUntil = cooldownUntil,
            now = now,
        )

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

    private fun recordQuotaRejection(lane: CristalixCredentialLane) {
        meterRegistry
            .counter(
                "mnemos.player_directory.cristalix.requests",
                "lane",
                lane.metricValue,
                "outcome",
                "local_quota_rejected",
            ).increment()
        meterRegistry
            .counter(
                "mnemos.player_directory.cristalix.quota_rejections",
                "lane",
                lane.metricValue,
            ).increment()
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

    private fun credential(lane: CristalixCredentialLane): CristalixApiProperties.Credential =
        requireNotNull(credentials[lane]) {
            "Cristalix credential lane $lane is not configured"
        }

    private fun quotaUnavailable(lane: CristalixCredentialLane): PlayerDirectoryUpstreamUnavailableException =
        PlayerDirectoryUpstreamUnavailableException(
            message = "Cristalix API quota is temporarily unavailable for $lane requests",
        )

    private val CristalixCredentialLane.metricValue: String
        get() = name.lowercase()

    private companion object {
        const val RETRY_AFTER_HEADER = "Retry-After"
        val DEFAULT_UPSTREAM_COOLDOWN: Duration = Duration.ofMinutes(1)
    }
}
