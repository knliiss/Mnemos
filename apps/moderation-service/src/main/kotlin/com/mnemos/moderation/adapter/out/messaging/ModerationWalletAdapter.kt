package com.mnemos.moderation.adapter.out.messaging

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.moderation.application.wallet.port.out.ModerationWallet
import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperation
import com.mnemos.wallet.contracts.wallet.WalletCommandType
import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.WalletOwner
import org.springframework.stereotype.Component

@Component
class ModerationWalletAdapter(
    private val publisher: IntegrationEventPublisher,
) : ModerationWallet {
    override fun requestAdjustment(operation: WalletAdjustmentOperation) {
        val command =
            WalletCreateTransactionCommand(
                requestId = operation.requestId,
                owner =
                    WalletOwner(
                        type = operation.targetType,
                        id = operation.targetId,
                    ),
                currency = operation.currency,
                type = operation.type,
                source = TransactionSource.ADMIN_ADJUSTMENT,
                sourceReferenceId = operation.id.id,
                amount = operation.amount,
            )

        publisher.publish(
            event =
                IntegrationEvent(
                    type = WalletCommandType.CREATE_TRANSACTION,
                    aggregateId = operation.requestId,
                    occurredAt = operation.createdAt,
                    payload = command,
                ),
            metadata = EventMetadata(actorUserId = operation.actorUserId.id),
        )
    }
}
