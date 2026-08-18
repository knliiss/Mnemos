package com.mnemos.social.adapter.inbound.messaging.wallet

import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.social.application.spotlight.model.SpotlightWalletResult
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionFailedEvent
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType

object SpotlightWalletResultMessageMapper {
    fun WalletTransactionCompletedEvent.toSpotlightResult(): SpotlightWalletResult? =
        if (source == TransactionSource.SPOTLIGHT_PURCHASE) {
            validateSpotlightResultShape()
            SpotlightWalletResult(
                requestId = requestId,
                spotlightId = sourceReferenceId,
                ownerUserId = owner.id,
                amount = amount,
            )
        } else {
            null
        }

    fun WalletTransactionFailedEvent.toSpotlightResult(): SpotlightWalletResult? =
        if (source == TransactionSource.SPOTLIGHT_PURCHASE) {
            validateSpotlightResultShape()
            SpotlightWalletResult(
                requestId = requestId,
                spotlightId = sourceReferenceId,
                ownerUserId = owner.id,
                amount = amount,
            )
        } else {
            null
        }

    private fun WalletTransactionCompletedEvent.validateSpotlightResultShape() {
        validateSpotlightResultShape(
            currency = currency,
            transactionType = type,
            ownerType = owner.type,
        )
    }

    private fun WalletTransactionFailedEvent.validateSpotlightResultShape() {
        validateSpotlightResultShape(
            currency = currency,
            transactionType = type,
            ownerType = owner.type,
        )
    }

    private fun validateSpotlightResultShape(
        currency: Currency,
        transactionType: TransactionType,
        ownerType: WalletOwnerType,
    ) {
        if (currency != Currency.PRIME) {
            throw KafkaMessageProtocolException("Spotlight wallet result currency must be PRIME")
        }

        if (transactionType != TransactionType.DEBIT) {
            throw KafkaMessageProtocolException("Spotlight wallet result type must be DEBIT")
        }

        if (ownerType != WalletOwnerType.USER) {
            throw KafkaMessageProtocolException("Spotlight wallet result owner must be USER")
        }
    }
}
