package com.mnemos.payment.adapter.out.account

import com.mnemos.contracts.account.internal.v1.InternalTelegramUserContextResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class PaymentTelegramUserResolverAdapterTest {
    @Test
    fun `resolver combines trusted telegram id with resolved user id`() {
        val userId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val client =
            PaymentAccountServiceClient { telegramId ->
                require(telegramId == 931458258L)

                InternalTelegramUserContextResponse(
                    userId = userId,
                    activeAccountId = null,
                )
            }
        val resolver = PaymentTelegramUserResolverAdapter(client)

        val result = requireNotNull(resolver.findByTelegramId(931458258L))

        assertEquals(userId, result.userId)
        assertEquals(931458258L, result.telegramId)
    }

    @Test
    fun `resolver returns null for unknown telegram user`() {
        val resolver = PaymentTelegramUserResolverAdapter(PaymentAccountServiceClient { null })

        assertNull(resolver.findByTelegramId(931458258L))
    }
}
