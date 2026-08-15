package com.mnemos.personalization.adapter.out.messaging

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.personalization.application.purchase.PurchaseWalletOwnerResolver
import com.mnemos.personalization.application.purchase.port.out.PersonalizationWallet
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import com.mnemos.wallet.contracts.wallet.WalletCommandType
import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import org.springframework.stereotype.Component

@Component
class PersonalizationWalletAdapter(
    private val publisher: IntegrationEventPublisher,
) : PersonalizationWallet {
    override fun requestDebit(operation: PurchaseOperation) {
        val command =
            WalletCreateTransactionCommand(
                requestId = operation.requestId,
                owner = PurchaseWalletOwnerResolver.resolve(operation),
                currency = operation.currency,
                type = TransactionType.DEBIT,
                source = TransactionSource.PERSONALIZATION_PURCHASE,
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
            metadata =
                EventMetadata(
                    actorUserId = operation.buyerUserId.id,
                ),
        )
    }
}
