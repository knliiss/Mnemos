package com.mnemos.bet.configuration

import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import org.springframework.boot.context.properties.ConfigurationProperties
import java.math.BigDecimal
import java.time.Duration

@ConfigurationProperties("mnemos.bet.event")
data class EventBetProperties(
    val closeBefore: Duration = Duration.ofMinutes(5),
    val resultGrace: Duration = Duration.ofMinutes(2),
    val scheduleHorizon: Duration = Duration.ofHours(12),
    val minimumBet: Long = 100,
    val maximumBet: Long = 5_000,
    val moonYesOdds: BigDecimal = BigDecimal("1.9000"),
    val moonNoOdds: BigDecimal = BigDecimal("1.9000"),
    val eclipseYesOdds: BigDecimal = BigDecimal("1.9000"),
    val eclipseNoOdds: BigDecimal = BigDecimal("1.9000"),
    val explosionYesOdds: BigDecimal = BigDecimal("1.9388"),
    val explosionNoOdds: BigDecimal = BigDecimal("1.8627"),
) {
    init {
        require(minimumBet > 0)
        require(maximumBet >= minimumBet)
    }

    fun oddsFor(eventType: DomainBetGlobalEventType): EventBetOdds =
        when (eventType) {
            DomainBetGlobalEventType.MOON -> EventBetOdds(moonYesOdds, moonNoOdds)
            DomainBetGlobalEventType.ECLIPSE -> EventBetOdds(eclipseYesOdds, eclipseNoOdds)
            DomainBetGlobalEventType.EXPLOSION -> EventBetOdds(explosionYesOdds, explosionNoOdds)
        }
}
