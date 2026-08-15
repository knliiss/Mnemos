package com.mnemos.personalization.application.revenue

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.configuration.revenue.CreatorRevenueProperties
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationStatus
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class CreatorRevenuePolicyTest {
    private val policy = CreatorRevenuePolicy(CreatorRevenueProperties())

    @Test
    fun `prime purchase pays seventy percent to creator`() {
        val operation = operation(Currency.PRIME, 100)

        assertEquals(70L, policy.calculatePrime(operation))
    }

    @Test
    fun `coin purchase converts smaller share to prime`() {
        val operation = operation(Currency.COIN, 10_000)

        assertEquals(50L, policy.calculatePrime(operation))
    }

    @Test
    fun `small prime purchase still produces positive creator revenue`() {
        val lowSharePolicy =
            CreatorRevenuePolicy(
                CreatorRevenueProperties(
                    primeSharePercent = 2,
                    coinSharePercent = 1,
                ),
            )
        val operation = operation(Currency.PRIME, 5)

        assertEquals(1L, lowSharePolicy.calculatePrime(operation))
    }

    @Test
    fun `percentage calculation does not overflow for large purchase amount`() {
        val fullSharePolicy =
            CreatorRevenuePolicy(
                CreatorRevenueProperties(
                    primeSharePercent = 100,
                    coinSharePercent = 99,
                ),
            )
        val operation = operation(Currency.PRIME, Long.MAX_VALUE)

        assertEquals(Long.MAX_VALUE, fullSharePolicy.calculatePrime(operation))
    }

    private fun operation(
        currency: Currency,
        amount: Long,
    ): PurchaseOperation =
        PurchaseOperation(
            id = PurchaseOperationId.generate(),
            requestId = UUID.randomUUID(),
            personalizationId = PersonalizationId.generate(),
            buyerUserId = UserId.generate(),
            owner = PersonalizationOwner.Account(AccountId.generate()),
            currency = currency,
            amount = amount,
            createdAt = Instant.parse("2026-08-15T03:00:00Z"),
            status = PurchaseOperationStatus.CONFIRMED,
            walletTransactionId = UUID.randomUUID(),
            failureReason = null,
        )
}
