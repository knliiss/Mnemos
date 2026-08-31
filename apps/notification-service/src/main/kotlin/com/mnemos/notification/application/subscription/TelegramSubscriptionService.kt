package com.mnemos.notification.application.subscription

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.application.common.pagination.PageQuery
import com.mnemos.notification.application.common.pagination.PageResult
import com.mnemos.notification.application.recipient.TelegramUserIdentityService
import com.mnemos.notification.application.subscription.model.SubscriptionView
import com.mnemos.notification.application.subscription.usecase.delete.DeleteSubscriptionCommand
import com.mnemos.notification.application.subscription.usecase.delete.DeleteSubscriptionUseCase
import com.mnemos.notification.application.subscription.usecase.list.ListSubscriptionsQuery
import com.mnemos.notification.application.subscription.usecase.list.ListSubscriptionsUseCase
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import org.springframework.stereotype.Service

@Service
class TelegramSubscriptionService(
    private val userIdentityService: TelegramUserIdentityService,
    private val listSubscriptionsUseCase: ListSubscriptionsUseCase,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCase,
) {
    fun list(
        telegramId: TelegramId,
        page: PageQuery,
    ): PageResult<SubscriptionView> {
        val userId = userIdentityService.resolve(telegramId) ?: return emptyPage(page)

        return listSubscriptionsUseCase.execute(
            ListSubscriptionsQuery(
                userId = userId,
                page = page,
            ),
        )
    }

    fun delete(
        telegramId: TelegramId,
        subscriptionId: SubscriptionId,
    ) {
        val userId = userIdentityService.resolve(telegramId) ?: return

        deleteSubscriptionUseCase.execute(
            DeleteSubscriptionCommand(
                subscriptionId = subscriptionId,
                userId = userId,
            ),
        )
    }

    private fun emptyPage(page: PageQuery): PageResult<SubscriptionView> =
        PageResult(
            items = emptyList(),
            page = page.page,
            size = page.size,
            totalItems = 0,
            totalPages = 0,
            hasNextPage = false,
        )
}
