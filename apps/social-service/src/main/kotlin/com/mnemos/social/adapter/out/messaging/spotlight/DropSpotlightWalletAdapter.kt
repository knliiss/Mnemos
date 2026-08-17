package com.mnemos.social.adapter.out.messaging.spotlight

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.social.application.spotlight.port.out.DropSpotlightWallet
import com.mnemos.social.domain.spotlight.model.DropSpotlight
import com.mnemos.wallet.contracts.wallet.WalletCommandType
import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwner
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.stereotype.Component

@Component
class DropSpotlightWalletAdapter(
    private val publisher: IntegrationEventPublisher,
) : DropSpotlightWallet {
    override fun requestDebit(spotlight: DropSpotlight) {
        publisher.publish(
            event =
                IntegrationEvent(
                    type = WalletCommandType.CREATE_TRANSACTION,
                    aggregateId = spotlight.walletRequestId,
                    occurredAt = spotlight.createdAt,
                    payload =
                        WalletCreateTransactionCommand(
                            requestId = spotlight.walletRequestId,
                            owner = WalletOwner(WalletOwnerType.USER, spotlight.buyerUserId),
                            currency = Currency.PRIME,
                            type = TransactionType.DEBIT,
                            source = TransactionSource.SPOTLIGHT_PURCHASE,
                            sourceReferenceId = spotlight.id.id,
                            amount = spotlight.pricePrime,
                        ),
                ),
            metadata = EventMetadata(actorUserId = spotlight.buyerUserId),
        )
    }
}
