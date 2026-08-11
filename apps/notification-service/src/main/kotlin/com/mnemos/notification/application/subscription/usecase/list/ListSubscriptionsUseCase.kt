package com.mnemos.notification.application.subscription.usecase.list

import com.mnemos.notification.application.common.pagination.PageResult
import com.mnemos.notification.application.subscription.model.SubscriptionView

fun interface ListSubscriptionsUseCase {
    fun execute(query: ListSubscriptionsQuery): PageResult<SubscriptionView>
}
