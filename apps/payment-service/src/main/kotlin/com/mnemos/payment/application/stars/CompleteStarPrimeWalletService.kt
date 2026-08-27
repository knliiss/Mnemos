package com.mnemos.payment.application.stars

import com.mnemos.payment.domain.stars.model.StarPrimePurchase
import com.mnemos.payment.domain.stars.repository.StarPrimePurchaseRepository
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionFailedEvent
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class CompleteStarPrimeWalletService(
    private val repository: StarPrimePurchaseRepository,
    private val clock: Clock,
) {
    @Transactional
    fun completed(event: WalletTransactionCompletedEvent) {
        if (event.source != TransactionSource.PRIME_PURCHASE) {
            return
        }

        val purchase = repository.findById(event.sourceReferenceId) ?: return

        requireWalletResult(
            userId = event.owner.id,
            ownerType = event.owner.type,
            currency = event.currency,
            type = event.type,
            amount = event.amount,
            purchase = purchase,
        )

        purchase.markFulfilled(clock.instant())
        repository.save(purchase)
    }

    @Transactional
    fun failed(event: WalletTransactionFailedEvent) {
        if (event.source != TransactionSource.PRIME_PURCHASE) {
            return
        }

        val purchase = repository.findById(event.sourceReferenceId) ?: return

        requireWalletResult(
            userId = event.owner.id,
            ownerType = event.owner.type,
            currency = event.currency,
            type = event.type,
            amount = event.amount,
            purchase = purchase,
        )

        purchase.markFulfillmentFailed(clock.instant())
        repository.save(purchase)
    }

    private fun requireWalletResult(
        userId: UUID,
        ownerType: WalletOwnerType,
        currency: Currency,
        type: TransactionType,
        amount: Long,
        purchase: StarPrimePurchase,
    ) {
        require(ownerType == WalletOwnerType.USER) {
            "PRIME purchase wallet result must target a user wallet"
        }
        require(userId == purchase.userId) {
            "PRIME purchase wallet result user does not match purchase"
        }
        require(currency == Currency.PRIME) {
            "PRIME purchase wallet result must use PRIME currency"
        }
        require(type == TransactionType.CREDIT) {
            "PRIME purchase wallet result must be a credit"
        }
        require(amount == purchase.primeAmount) {
            "PRIME purchase wallet result amount does not match purchase"
        }
    }
}
