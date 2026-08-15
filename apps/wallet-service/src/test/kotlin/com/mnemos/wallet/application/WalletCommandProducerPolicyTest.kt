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

class WalletCommandProducerPolicyTest {
    private val policy = WalletCommandProducerPolicy()

    @Test
    fun `bet service can use only account coin`() {
        assertDoesNotThrow {
            policy.requireAllowed(
                producer = "bet-service",
                command =
                    command(
                        ownerType = WalletOwnerType.ACCOUNT,
                        currency = Currency.COIN,
                        source = TransactionSource.BET_PLACE,
                        type = TransactionType.DEBIT,
                    ),
                actorUserId = UserId.generate().id,
            )
        }

        assertThrows(UnauthorizedWalletCommandException::class.java) {
            policy.requireAllowed(
                producer = "bet-service",
                command =
                    command(
                        ownerType = WalletOwnerType.USER,
                        currency = Currency.PRIME,
                        source = TransactionSource.BET_PLACE,
                        type = TransactionType.DEBIT,
                    ),
                actorUserId = UserId.generate().id,
            )
        }
    }

    @Test
    fun `reward service can credit coin to account and prime to user`() {
        assertDoesNotThrow {
            policy.requireAllowed(
                producer = "reward-service",
                command =
                    command(
                        ownerType = WalletOwnerType.ACCOUNT,
                        currency = Currency.COIN,
                        source = TransactionSource.DAILY_REWARD,
                        type = TransactionType.CREDIT,
                    ),
                actorUserId = null,
            )
        }

        assertDoesNotThrow {
            policy.requireAllowed(
                producer = "reward-service",
                command =
                    command(
                        ownerType = WalletOwnerType.USER,
                        currency = Currency.PRIME,
                        source = TransactionSource.DAILY_REWARD,
                        type = TransactionType.CREDIT,
                    ),
                actorUserId = null,
            )
        }
    }

    @Test
    fun `personalization purchase supports account coin and user prime only`() {
        assertDoesNotThrow {
            policy.requireAllowed(
                producer = "personalization-service",
                command =
                    command(
                        ownerType = WalletOwnerType.ACCOUNT,
                        currency = Currency.COIN,
                        source = TransactionSource.PERSONALIZATION_PURCHASE,
                        type = TransactionType.DEBIT,
                    ),
                actorUserId = UserId.generate().id,
            )
        }

        assertDoesNotThrow {
            policy.requireAllowed(
                producer = "personalization-service",
                command =
                    command(
                        ownerType = WalletOwnerType.USER,
                        currency = Currency.PRIME,
                        source = TransactionSource.PERSONALIZATION_PURCHASE,
                        type = TransactionType.DEBIT,
                    ),
                actorUserId = UserId.generate().id,
            )
        }

        assertThrows(UnauthorizedWalletCommandException::class.java) {
            policy.requireAllowed(
                producer = "personalization-service",
                command =
                    command(
                        ownerType = WalletOwnerType.ACCOUNT,
                        currency = Currency.PRIME,
                        source = TransactionSource.PERSONALIZATION_PURCHASE,
                        type = TransactionType.DEBIT,
                    ),
                actorUserId = UserId.generate().id,
            )
        }

        assertThrows(UnauthorizedWalletCommandException::class.java) {
            policy.requireAllowed(
                producer = "personalization-service",
                command =
                    command(
                        ownerType = WalletOwnerType.USER,
                        currency = Currency.COIN,
                        source = TransactionSource.PERSONALIZATION_PURCHASE,
                        type = TransactionType.DEBIT,
                    ),
                actorUserId = UserId.generate().id,
            )
        }
    }

    @Test
    fun `creator revenue supports only prime credit to user`() {
        assertDoesNotThrow {
            policy.requireAllowed(
                producer = "personalization-service",
                command =
                    command(
                        ownerType = WalletOwnerType.USER,
                        currency = Currency.PRIME,
                        source = TransactionSource.CREATOR_REVENUE,
                        type = TransactionType.CREDIT,
                    ),
                actorUserId = null,
            )
        }

        assertThrows(UnauthorizedWalletCommandException::class.java) {
            policy.requireAllowed(
                producer = "personalization-service",
                command =
                    command(
                        ownerType = WalletOwnerType.ACCOUNT,
                        currency = Currency.COIN,
                        source = TransactionSource.CREATOR_REVENUE,
                        type = TransactionType.CREDIT,
                    ),
                actorUserId = null,
            )
        }
    }

    private fun command(
        ownerType: WalletOwnerType,
        currency: Currency,
        source: TransactionSource,
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
            source = source,
            sourceReferenceId = AccountId.generate().id,
            amount = 100,
        )
}
