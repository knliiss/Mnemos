package com.mnemos.wallet.application.transaction.policy

import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class WalletCommandProducerPolicy {
    fun requireAllowed(
        producer: String,
        command: WalletCreateTransactionCommand,
        actorUserId: UUID?,
    ) {
        val allowed =
            when (producer) {
                "reward-service" -> command.isReward()
                "bet-service" -> command.isBetOperation()
                "personalization-service" -> command.isPersonalizationOperation()
                "social-service" -> command.isSocialOperation()
                "payment-service" -> command.isPrimePurchase()
                "administration-service" -> command.isAdminAdjustment(actorUserId)
                else -> false
            }

        if (!allowed) {
            throw UnauthorizedWalletCommandException(
                producer = producer,
                source = command.source,
                type = command.type,
                currency = command.currency,
            )
        }
    }

    private fun WalletCreateTransactionCommand.isReward(): Boolean =
        when (source) {
            TransactionSource.DAILY_REWARD ->
                type == TransactionType.CREDIT &&
                    (
                        (currency == Currency.COIN && owner.type == WalletOwnerType.ACCOUNT) ||
                            (currency == Currency.PRIME && owner.type == WalletOwnerType.USER)
                    )

            TransactionSource.ITEM_DROP_REWARD ->
                type == TransactionType.CREDIT &&
                    currency == Currency.COIN &&
                    owner.type == WalletOwnerType.ACCOUNT

            else -> false
        }

    private fun WalletCreateTransactionCommand.isBetOperation(): Boolean =
        owner.type == WalletOwnerType.ACCOUNT &&
            currency == Currency.COIN &&
            when (source) {
                TransactionSource.BET_PLACE -> type == TransactionType.DEBIT
                TransactionSource.BET_WIN,
                TransactionSource.BET_REFUND,
                -> type == TransactionType.CREDIT
                else -> false
            }

    private fun WalletCreateTransactionCommand.isPersonalizationOperation(): Boolean =
        when (source) {
            TransactionSource.PERSONALIZATION_PURCHASE -> type == TransactionType.DEBIT
            TransactionSource.CREATOR_REVENUE ->
                type == TransactionType.CREDIT &&
                    currency == Currency.PRIME &&
                    owner.type == WalletOwnerType.USER
            else -> false
        }

    private fun WalletCreateTransactionCommand.isSocialOperation(): Boolean =
        source == TransactionSource.SPOTLIGHT_PURCHASE &&
            type == TransactionType.DEBIT &&
            currency == Currency.PRIME &&
            owner.type == WalletOwnerType.USER

    private fun WalletCreateTransactionCommand.isPrimePurchase(): Boolean =
        source == TransactionSource.PRIME_PURCHASE &&
            type == TransactionType.CREDIT &&
            currency == Currency.PRIME &&
            owner.type == WalletOwnerType.USER

    private fun WalletCreateTransactionCommand.isAdminAdjustment(actorUserId: UUID?): Boolean =
        source == TransactionSource.ADMIN_ADJUSTMENT && actorUserId != null
}
