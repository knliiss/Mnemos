package com.mnemos.bet.adapter.out.messaging

import com.mnemos.bet.application.wallet.port.out.BetWallet
import com.mnemos.bet.domain.wallet.model.WalletOperation
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.wallet.contracts.wallet.WalletCommandType
import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwner
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.stereotype.Component

@Component
class BetWalletAdapter(
    private val publisher: IntegrationEventPublisher,
) : BetWallet {
    override fun request(operation: WalletOperation) {
        val type =
            when (operation.kind) {
                WalletOperationKind.STAKE -> TransactionType.DEBIT
                WalletOperationKind.PAYOUT,
                WalletOperationKind.REFUND,
                -> TransactionType.CREDIT
            }

        val source =
            when (operation.kind) {
                WalletOperationKind.STAKE -> TransactionSource.BET_PLACE
                WalletOperationKind.PAYOUT -> TransactionSource.BET_WIN
                WalletOperationKind.REFUND -> TransactionSource.BET_REFUND
            }

        val command =
            WalletCreateTransactionCommand(
                requestId = operation.requestId.id,
                owner =
                    WalletOwner(
                        type = WalletOwnerType.ACCOUNT,
                        id = operation.accountId.id,
                    ),
                currency = Currency.COIN,
                type = type,
                source = source,
                sourceReferenceId = operation.id.id,
                amount = operation.amount,
            )

        publisher.publish(
            event =
                IntegrationEvent(
                    type = WalletCommandType.CREATE_TRANSACTION,
                    aggregateId = operation.requestId.id,
                    occurredAt = operation.createdAt,
                    payload = command,
                ),
            metadata =
                EventMetadata(
                    actorUserId = operation.initiatedByUserId?.id,
                ),
        )
    }
}
