package com.mnemos.wallet.application

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.wallet.application.transaction.policy.UnauthorizedWalletCommandException
import com.mnemos.wallet.application.transaction.policy.WalletCommandProducerPolicy
import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwner
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import com.mnemos.wallet.domain.transaction.model.RequestId
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ModerationWalletCommandProducerPolicyTest {
    private val policy = WalletCommandProducerPolicy()

    @Test
    fun `moderation service can adjust account coin`() {
        val command =
            command(
                ownerType = WalletOwnerType.ACCOUNT,
                currency = Currency.COIN,
                type = TransactionType.CREDIT,
            )

        assertDoesNotThrow {
            policy.requireAllowed(
                producer = "moderation-service",
                command = command,
                actorUserId = UserId.generate().id,
            )
        }
    }

    @Test
    fun `moderation service can adjust user prime`() {
        val command =
            command(
                ownerType = WalletOwnerType.USER,
                currency = Currency.PRIME,
                type = TransactionType.DEBIT,
            )

        assertDoesNotThrow {
            policy.requireAllowed(
                producer = "moderation-service",
                command = command,
                actorUserId = UserId.generate().id,
            )
        }
    }

    @Test
    fun `moderation service cannot send invalid owner currency pair`() {
        val command =
            command(
                ownerType = WalletOwnerType.USER,
                currency = Currency.COIN,
                type = TransactionType.CREDIT,
            )

        assertThrows(UnauthorizedWalletCommandException::class.java) {
            policy.requireAllowed(
                producer = "moderation-service",
                command = command,
                actorUserId = UserId.generate().id,
            )
        }
    }

    @Test
    fun `moderation adjustment requires actor`() {
        val command =
            command(
                ownerType = WalletOwnerType.ACCOUNT,
                currency = Currency.COIN,
                type = TransactionType.CREDIT,
            )

        assertThrows(UnauthorizedWalletCommandException::class.java) {
            policy.requireAllowed(
                producer = "moderation-service",
                command = command,
                actorUserId = null,
            )
        }
    }

    private fun command(
        ownerType: WalletOwnerType,
        currency: Currency,
        type: TransactionType,
    ): WalletCreateTransactionCommand =
        WalletCreateTransactionCommand(
            requestId = RequestId.generate().id,
            owner =
                WalletOwner(
                    type = ownerType,
                    id =
                        if (ownerType == WalletOwnerType.ACCOUNT) {
                            AccountId.generate().id
                        } else {
                            UserId.generate().id
                        },
                ),
            currency = currency,
            type = type,
            source = TransactionSource.ADMIN_ADJUSTMENT,
            sourceReferenceId = AccountId.generate().id,
            amount = 100,
        )
}
