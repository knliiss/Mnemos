package com.mnemos.moderation.application.sanction.mapper

import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.moderation.contracts.ModerationEventType
import com.mnemos.moderation.contracts.event.SanctionStateV1
import com.mnemos.moderation.contracts.shared.SanctionStatus
import com.mnemos.moderation.contracts.shared.SanctionType
import com.mnemos.moderation.domain.sanction.model.DomainSanctionStatus
import com.mnemos.moderation.domain.sanction.model.DomainSanctionType
import com.mnemos.moderation.domain.sanction.model.Sanction
import java.time.Instant

object SanctionIntegrationEventMapper {
    fun Sanction.toActivatedIntegrationEvent(occurredAt: Instant): IntegrationEvent<SanctionStateV1> =
        toIntegrationEvent(
            eventType = ModerationEventType.SANCTION_ACTIVATED,
            occurredAt = occurredAt,
        )

    fun Sanction.toDeactivatedIntegrationEvent(occurredAt: Instant): IntegrationEvent<SanctionStateV1> =
        toIntegrationEvent(
            eventType = ModerationEventType.SANCTION_DEACTIVATED,
            occurredAt = occurredAt,
        )

    private fun Sanction.toIntegrationEvent(
        eventType: String,
        occurredAt: Instant,
    ): IntegrationEvent<SanctionStateV1> =
        IntegrationEvent(
            type = eventType,
            aggregateId = id.id,
            occurredAt = occurredAt,
            payload =
                SanctionStateV1(
                    sanctionId = id.id,
                    userId = userId.id,
                    type = type.toContract(),
                    status = status.toContract(),
                    occurredAt = occurredAt,
                ),
        )

    private fun DomainSanctionType.toContract(): SanctionType = SanctionType.valueOf(name)

    private fun DomainSanctionStatus.toContract(): SanctionStatus = SanctionStatus.valueOf(name)
}
