package com.mnemos.payment.adapter.out.messaging.wallet

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.payment.application.stars.port.out.StarPrimeWallet
import com.mnemos.payment.domain.stars.model.StarPrimePurchase
import com.mnemos.wallet.contracts.wallet.WalletCommandType
import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwner
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.stereotype.Component

@Component
class StarPrimeWalletAdapter(
    private val publisher: IntegrationEventPublisher,
) : StarPrimeWallet {
    override fun requestCredit(purchase: StarPrimePurchase) {
        val command =
            WalletCreateTransactionCommand(
                requestId = purchase.id,
                owner =
                    WalletOwner(
                        type = WalletOwnerType.USER,
                        id = purchase.userId,
                    ),
                currency = Currency.PRIME,
                type = TransactionType.CREDIT,
                source = TransactionSource.PRIME_PURCHASE,
                sourceReferenceId = purchase.id,
                amount = purchase.primeAmount,
            )

        publisher.publish(
            event =
                IntegrationEvent(
                    type = WalletCommandType.CREATE_TRANSACTION,
                    aggregateId = purchase.id,
                    occurredAt = requireNotNull(purchase.paidAt),
                    payload = command,
                ),
            metadata = EventMetadata(actorUserId = purchase.userId),
        )
    }
}
