package com.mnemos.bet.domain.eventbet.model

import java.math.BigDecimal
import java.time.Instant

class EventBetRound private constructor(
    val id: EventBetRoundId,
    val eventType: DomainBetGlobalEventType,
    val scheduledAt: Instant,
    val closesAt: Instant,
    val resolvesAt: Instant,
    val yesOdds: BigDecimal,
    val noOdds: BigDecimal,
    status: EventBetRoundStatus,
    effectiveOutcome: EventBetOutcome?,
) {
    var status = status
        private set

    var effectiveOutcome = effectiveOutcome
        private set

    companion object {
        fun create(
            eventType: DomainBetGlobalEventType,
            scheduledAt: Instant,
            closesAt: Instant,
            resolvesAt: Instant,
            yesOdds: BigDecimal,
            noOdds: BigDecimal,
        ): EventBetRound {
            require(closesAt.isBefore(scheduledAt))
            require(resolvesAt.isAfter(scheduledAt))
            require(yesOdds > BigDecimal.ONE)
            require(noOdds > BigDecimal.ONE)

            return EventBetRound(
                id = EventBetRoundId.generate(),
                eventType = eventType,
                scheduledAt = scheduledAt,
                closesAt = closesAt,
                resolvesAt = resolvesAt,
                yesOdds = yesOdds,
                noOdds = noOdds,
                status = EventBetRoundStatus.OPEN,
                effectiveOutcome = null,
            )
        }

        fun restore(
            id: EventBetRoundId,
            eventType: DomainBetGlobalEventType,
            scheduledAt: Instant,
            closesAt: Instant,
            resolvesAt: Instant,
            yesOdds: BigDecimal,
            noOdds: BigDecimal,
            status: EventBetRoundStatus,
            effectiveOutcome: EventBetOutcome?,
        ): EventBetRound =
            EventBetRound(
                id = id,
                eventType = eventType,
                scheduledAt = scheduledAt,
                closesAt = closesAt,
                resolvesAt = resolvesAt,
                yesOdds = yesOdds,
                noOdds = noOdds,
                status = status,
                effectiveOutcome = effectiveOutcome,
            )
    }

    fun synchronize(now: Instant) {
        if (status == EventBetRoundStatus.OPEN && !now.isBefore(closesAt)) {
            status = EventBetRoundStatus.LOCKED
        }

        if (status == EventBetRoundStatus.LOCKED && !now.isBefore(scheduledAt)) {
            status = EventBetRoundStatus.AWAITING_RESULT
        }
    }

    fun requireBettingOpen(now: Instant) {
        check(status == EventBetRoundStatus.OPEN && now.isBefore(closesAt)) {
            "Betting is closed"
        }
    }

    fun awaitAdministrativeConfirmation() {
        check(effectiveOutcome == null)
        status = EventBetRoundStatus.AWAITING_ADMIN_CONFIRMATION
    }

    fun resolveAutomatically(outcome: EventBetOutcome) {
        check(outcome == EventBetOutcome.YES || outcome == EventBetOutcome.NO)

        when (outcome) {
            EventBetOutcome.YES ->
                check(
                    status == EventBetRoundStatus.AWAITING_RESULT ||
                        status == EventBetRoundStatus.AWAITING_ADMIN_CONFIRMATION,
                )

            EventBetOutcome.NO -> check(status == EventBetRoundStatus.AWAITING_RESULT)
            EventBetOutcome.REFUND -> error("REFUND cannot be resolved automatically")
        }

        effectiveOutcome = outcome
        status = EventBetRoundStatus.SETTLED
    }

    fun resolveAdministratively(outcome: EventBetOutcome) {
        if (status != EventBetRoundStatus.SETTLED) {
            check(
                status == EventBetRoundStatus.AWAITING_ADMIN_CONFIRMATION ||
                    outcome == EventBetOutcome.REFUND,
            ) {
                "YES or NO can be confirmed manually only after automatic confirmation becomes unavailable"
            }
        }

        effectiveOutcome = outcome
        status = EventBetRoundStatus.SETTLED
    }

    fun oddsFor(side: DomainBetSide): BigDecimal =
        when (side) {
            DomainBetSide.YES -> yesOdds
            DomainBetSide.NO -> noOdds
        }
}
