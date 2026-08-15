package com.mnemos.personalization.adapter.out.messaging

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.personalization.application.revenue.port.out.CreatorRevenueWallet
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation
import com.mnemos.wallet.contracts.wallet.WalletCommandType
import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwner
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class CreatorRevenueWalletAdapter(
    private val publisher: IntegrationEventPublisher,
    private val clock: Clock,
) : CreatorRevenueWallet {
    override fun requestCredit(operation: CreatorRevenueOperation) {
        val command =
            WalletCreateTransactionCommand(
                requestId = operation.requestId,
                owner =
                    WalletOwner(
                        type = WalletOwnerType.USER,
                        id = operation.creatorUserId.id,
                    ),
                currency = Currency.PRIME,
                type = TransactionType.CREDIT,
                source = TransactionSource.CREATOR_REVENUE,
                sourceReferenceId = operation.id.id,
                amount = operation.amountPrime,
            )

        publisher.publish(
            event =
                IntegrationEvent(
                    type = WalletCommandType.CREATE_TRANSACTION,
                    aggregateId = operation.requestId,
                    occurredAt = clock.instant(),
                    payload = command,
                ),
            metadata = EventMetadata(),
        )
    }
}
