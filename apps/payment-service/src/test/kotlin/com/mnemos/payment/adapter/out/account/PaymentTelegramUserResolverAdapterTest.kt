package com.mnemos.payment.adapter.out.account

import com.mnemos.contracts.account.internal.v1.InternalTelegramUserIdentityResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class PaymentTelegramUserResolverAdapterTest {
    private val client = mock<PaymentAccountServiceClient>()
    private val resolver = PaymentTelegramUserResolverAdapter(client)

    @Test
    fun `resolver combines trusted telegram id with resolved user id`() {
        val userId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")

        whenever(client.getTelegramUserIdentity(TELEGRAM_ID)).thenReturn(
            InternalTelegramUserIdentityResponse(
                userId = userId,
            ),
        )

        val result = requireNotNull(resolver.findByTelegramId(TELEGRAM_ID))

        assertEquals(userId, result.userId)
        assertEquals(TELEGRAM_ID, result.telegramId)
    }

    @Test
    fun `resolver returns null for unknown telegram user`() {
        whenever(client.getTelegramUserIdentity(TELEGRAM_ID)).thenReturn(null)

        assertNull(resolver.findByTelegramId(TELEGRAM_ID))
    }

    @Test
    fun `resolver bootstraps telegram user when requested`() {
        val userId = UUID.fromString("019c113f-b116-7000-8000-000000000202")

        whenever(client.bootstrapTelegramUserIdentity(TELEGRAM_ID)).thenReturn(
            InternalTelegramUserIdentityResponse(
                userId = userId,
            ),
        )

        val result = resolver.getOrCreateByTelegramId(TELEGRAM_ID)

        assertEquals(userId, result.userId)
        assertEquals(TELEGRAM_ID, result.telegramId)
    }

    private companion object {
        const val TELEGRAM_ID = 931458258L
    }
}
