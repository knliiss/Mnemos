package com.mnemos.personalization.adapter.inbound.messaging.wallet

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.personalization.application.wallet.model.WalletResultContext
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionFailedEvent

object WalletResultContextMapper {
    fun WalletTransactionCompletedEvent.toResultContext(message: ConsumedMessage<WalletTransactionCompletedEvent>): WalletResultContext =
        WalletResultContext(
            requestId = requestId,
            actorUserId = message.metadata.actorUserId,
            ownerType = owner.type,
            ownerId = owner.id,
            currency = currency,
            transactionType = type,
            source = source,
            sourceReferenceId = sourceReferenceId,
            amount = amount,
        )

    fun WalletTransactionFailedEvent.toResultContext(message: ConsumedMessage<WalletTransactionFailedEvent>): WalletResultContext =
        WalletResultContext(
            requestId = requestId,
            actorUserId = message.metadata.actorUserId,
            ownerType = owner.type,
            ownerId = owner.id,
            currency = currency,
            transactionType = type,
            source = source,
            sourceReferenceId = sourceReferenceId,
            amount = amount,
        )
}
