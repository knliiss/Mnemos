package com.mnemos.notification.application.subscription

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.common.pagination.PageQuery
import com.mnemos.notification.application.common.pagination.PageResult
import com.mnemos.notification.application.recipient.model.RecipientView
import com.mnemos.notification.application.recipient.port.out.RecipientReader
import com.mnemos.notification.application.subscription.model.SubscriptionView
import com.mnemos.notification.application.subscription.usecase.delete.DeleteSubscriptionCommand
import com.mnemos.notification.application.subscription.usecase.delete.DeleteSubscriptionUseCase
import com.mnemos.notification.application.subscription.usecase.list.ListSubscriptionsQuery
import com.mnemos.notification.application.subscription.usecase.list.ListSubscriptionsUseCase
import com.mnemos.notification.domain.recipient.model.RecipientPriority
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class TelegramSubscriptionServiceTest {
    @Test
    fun `unknown Telegram recipient returns empty page without loading subscriptions`() {
        var listCalled = false
        val service =
            TelegramSubscriptionService(
                recipientReader = FixedRecipientReader(null),
                listSubscriptionsUseCase =
                    ListSubscriptionsUseCase {
                        listCalled = true
                        emptyPage()
                    },
                deleteSubscriptionUseCase = DeleteSubscriptionUseCase { },
            )

        val result =
            service.list(
                telegramId = TELEGRAM_ID,
                page = PageQuery(page = 3, size = 8),
            )

        assertEquals(3, result.page)
        assertEquals(8, result.size)
        assertEquals(0, result.totalItems)
        assertFalse(result.hasNextPage)
        assertFalse(listCalled)
    }

    @Test
    fun `known Telegram recipient delegates list with resolved user id`() {
        var capturedQuery: ListSubscriptionsQuery? = null
        val expectedResult = emptyPage(page = 1, size = 8)
        val service =
            TelegramSubscriptionService(
                recipientReader = FixedRecipientReader(recipient()),
                listSubscriptionsUseCase =
                    ListSubscriptionsUseCase { query ->
                        capturedQuery = query
                        expectedResult
                    },
                deleteSubscriptionUseCase = DeleteSubscriptionUseCase { },
            )

        val result =
            service.list(
                telegramId = TELEGRAM_ID,
                page = PageQuery(page = 1, size = 8),
            )

        assertEquals(expectedResult, result)
        assertEquals(USER_ID, capturedQuery?.userId)
        assertEquals(1, capturedQuery?.page?.page)
        assertEquals(8, capturedQuery?.page?.size)
    }

    @Test
    fun `delete resolves Telegram recipient before delegating`() {
        var capturedCommand: DeleteSubscriptionCommand? = null
        val subscriptionId = SubscriptionId(UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71"))
        val service =
            TelegramSubscriptionService(
                recipientReader = FixedRecipientReader(recipient()),
                listSubscriptionsUseCase = ListSubscriptionsUseCase { emptyPage() },
                deleteSubscriptionUseCase =
                    DeleteSubscriptionUseCase { command ->
                        capturedCommand = command
                    },
            )

        service.delete(
            telegramId = TELEGRAM_ID,
            subscriptionId = subscriptionId,
        )

        assertEquals(subscriptionId, capturedCommand?.subscriptionId)
        assertEquals(USER_ID, capturedCommand?.userId)
    }

    @Test
    fun `delete ignores unknown Telegram recipient`() {
        var capturedCommand: DeleteSubscriptionCommand? = null
        val service =
            TelegramSubscriptionService(
                recipientReader = FixedRecipientReader(null),
                listSubscriptionsUseCase = ListSubscriptionsUseCase { emptyPage() },
                deleteSubscriptionUseCase =
                    DeleteSubscriptionUseCase { command ->
                        capturedCommand = command
                    },
            )

        service.delete(
            telegramId = TELEGRAM_ID,
            subscriptionId = SubscriptionId(UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")),
        )

        assertNull(capturedCommand)
    }

    private fun recipient(): RecipientView =
        RecipientView(
            userId = USER_ID,
            telegramId = TELEGRAM_ID,
            priority = RecipientPriority.STANDARD,
        )

    private fun emptyPage(
        page: Int = 0,
        size: Int = 8,
    ): PageResult<SubscriptionView> =
        PageResult(
            items = emptyList(),
            page = page,
            size = size,
            totalItems = 0,
            totalPages = 0,
            hasNextPage = false,
        )

    private class FixedRecipientReader(
        private val recipient: RecipientView?,
    ) : RecipientReader {
        override fun findByUserIds(userIds: Set<UserId>): Map<UserId, RecipientView> = emptyMap()

        override fun findByTelegramId(telegramId: TelegramId): RecipientView? = recipient
    }

    private companion object {
        val USER_ID = UserId(UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72"))
        val TELEGRAM_ID = TelegramId(931458258L)
    }
}
