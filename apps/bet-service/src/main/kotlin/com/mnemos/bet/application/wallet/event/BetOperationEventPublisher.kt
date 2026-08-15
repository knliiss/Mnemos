package com.mnemos.bet.application.wallet.event

import com.mnemos.bet.contracts.BetEventType
import com.mnemos.bet.contracts.event.BetOperationCompletedV1
import com.mnemos.bet.contracts.shared.BetOperationResourceType
import com.mnemos.bet.contracts.shared.BetOperationStatus
import com.mnemos.bet.domain.wallet.model.WalletOperation
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.stereotype.Component
import java.time.Clock
import java.util.UUID

@Component
class BetOperationEventPublisher(
    private val publisher: IntegrationEventPublisher,
    private val clock: Clock,
) {
    fun publish(
        operation: WalletOperation,
        status: BetOperationStatus,
        failureReason: String? = null,
    ) {
        val userId = operation.initiatedByUserId ?: return

        if (operation.kind != WalletOperationKind.STAKE) {
            return
        }

        val occurredAt = clock.instant()
        val resource = operation.resolveResource()

        publisher.publish(
            event =
                IntegrationEvent(
                    type = BetEventType.OPERATION_COMPLETED,
                    aggregateId = operation.id.id,
                    occurredAt = occurredAt,
                    payload =
                        BetOperationCompletedV1(
                            operationId = operation.id.id,
                            userId = userId.id,
                            resourceType = resource.type,
                            resourceId = resource.id,
                            status = status,
                            amount = operation.amount,
                            walletTransactionId = operation.walletTransactionId?.id,
                            failureReason = failureReason,
                            occurredAt = occurredAt,
                        ),
                ),
            metadata =
                EventMetadata(
                    actorUserId = userId.id,
                ),
        )
    }

    private fun WalletOperation.resolveResource(): OperationResource =
        when {
            eventBetId != null ->
                OperationResource(
                    type = BetOperationResourceType.EVENT_BET,
                    id = eventBetId.id,
                )

            raceParticipantId != null ->
                OperationResource(
                    type = BetOperationResourceType.ITEM_RACE_PARTICIPANT,
                    id = raceParticipantId.id,
                )

            else -> error("Wallet operation ${id.id} does not reference a bet resource")
        }

    private data class OperationResource(
        val type: BetOperationResourceType,
        val id: UUID,
    )
}
