package com.mnemos.wallet.application.exchange

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.wallet.application.account.port.out.ActiveAccountProvider
import com.mnemos.wallet.application.security.port.out.CurrentUserProvider
import com.mnemos.wallet.application.transaction.usecase.create.CreateTransactionRequest
import com.mnemos.wallet.application.transaction.usecase.create.CreateTransactionResult
import com.mnemos.wallet.application.transaction.usecase.create.CreateTransactionService
import com.mnemos.wallet.configuration.CoinPurchaseProperties
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import com.mnemos.wallet.domain.transaction.model.RequestId
import com.mnemos.wallet.domain.transaction.model.TransactionSourceReferenceId
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.util.UUID

@Service
class PurchaseCoinService(
    private val currentUserProvider: CurrentUserProvider,
    private val activeAccountProvider: ActiveAccountProvider,
    private val createTransactionService: CreateTransactionService,
    private val properties: CoinPurchaseProperties,
) {
    @Transactional(readOnly = true)
    fun catalog(accountId: AccountId): CoinPurchaseCatalog {
        requireActiveAccount(accountId)

        val coinPerPrime = requireCoinPerPrime()
        val options =
            properties
                .normalizedPrimeAmounts()
                .map { primeAmount ->
                    CoinPurchaseOption(
                        primeAmount = primeAmount,
                        coinAmount = Math.multiplyExact(primeAmount, coinPerPrime),
                    )
                }

        return CoinPurchaseCatalog(
            coinPerPrime = coinPerPrime,
            options = options,
        )
    }

    @Transactional
    fun purchase(
        accountId: AccountId,
        requestId: UUID,
        primeAmount: Long,
    ): CoinPurchaseOption {
        val userId = currentUserProvider.requireUserId()
        val activeAccountId = activeAccountProvider.findActiveAccountId(userId)

        if (activeAccountId != accountId) {
            throw AppException(
                message = "COIN can only be purchased for the active account",
                code = "ACCOUNT_PERMISSION_DENIED",
                httpStatus = HttpStatus.FORBIDDEN,
                details = mapOf("accountId" to accountId.id),
            )
        }

        val coinPerPrime = requireCoinPerPrime()
        val allowedPrimeAmounts = properties.normalizedPrimeAmounts()

        if (primeAmount !in allowedPrimeAmounts) {
            throw AppException(
                message = "Unsupported PRIME amount for COIN purchase",
                code = "COIN_PURCHASE_OPTION_INVALID",
                httpStatus = HttpStatus.BAD_REQUEST,
                details = mapOf("primeAmount" to primeAmount),
            )
        }

        val coinAmount = Math.multiplyExact(primeAmount, coinPerPrime)
        val sourceReferenceId = TransactionSourceReferenceId(requestId)
        val metadata = EventMetadata(actorUserId = userId.id)

        requireCompleted(
            createTransactionService.create(
                request =
                    CreateTransactionRequest(
                        requestId = derivedRequestId(requestId, "prime-debit"),
                        owner = WalletOwner.User(userId),
                        currency = Currency.PRIME,
                        type = TransactionType.DEBIT,
                        source = TransactionSource.COIN_PURCHASE,
                        sourceReferenceId = sourceReferenceId,
                        amount = primeAmount,
                    ),
                metadata = metadata,
            ),
        )

        requireCompleted(
            createTransactionService.create(
                request =
                    CreateTransactionRequest(
                        requestId = derivedRequestId(requestId, "coin-credit"),
                        owner = WalletOwner.Account(accountId),
                        currency = Currency.COIN,
                        type = TransactionType.CREDIT,
                        source = TransactionSource.COIN_PURCHASE,
                        sourceReferenceId = sourceReferenceId,
                        amount = coinAmount,
                    ),
                metadata = metadata,
            ),
        )

        return CoinPurchaseOption(
            primeAmount = primeAmount,
            coinAmount = coinAmount,
        )
    }

    private fun requireActiveAccount(accountId: AccountId) {
        val userId = currentUserProvider.requireUserId()
        val activeAccountId = activeAccountProvider.findActiveAccountId(userId)

        if (activeAccountId != accountId) {
            throw AppException(
                message = "COIN purchase catalog is only available for the active account",
                code = "ACCOUNT_PERMISSION_DENIED",
                httpStatus = HttpStatus.FORBIDDEN,
                details = mapOf("accountId" to accountId.id),
            )
        }
    }

    private fun requireCoinPerPrime(): Long {
        val rate = properties.coinPerPrime

        if (rate <= 0) {
            throw IllegalStateException("mnemos.wallet.coin-purchase.coin-per-prime must be positive")
        }

        return rate
    }

    private fun requireCompleted(result: CreateTransactionResult) {
        if (result is CreateTransactionResult.Completed) {
            return
        }

        val failure = result as CreateTransactionResult.Failed

        when (failure.reason) {
            WalletTransactionFailureReason.INSUFFICIENT_BALANCE ->
                throw AppException(
                    message = "Insufficient PRIME balance",
                    code = "INSUFFICIENT_BALANCE",
                    httpStatus = HttpStatus.BAD_REQUEST,
                )

            WalletTransactionFailureReason.IDEMPOTENCY_CONFLICT ->
                throw AppException(
                    message = "Coin purchase request conflicts with an existing request",
                    code = "IDEMPOTENCY_CONFLICT",
                    httpStatus = HttpStatus.CONFLICT,
                )

            else ->
                throw AppException(
                    message = "Coin purchase could not be completed",
                    code = "COIN_PURCHASE_FAILED",
                    httpStatus = HttpStatus.CONFLICT,
                    details = mapOf("reason" to failure.reason.name),
                )
        }
    }

    private fun derivedRequestId(
        purchaseRequestId: UUID,
        leg: String,
    ): RequestId {
        val value = "$purchaseRequestId:$leg".toByteArray(StandardCharsets.UTF_8)

        return RequestId(UUID.nameUUIDFromBytes(value))
    }
}
