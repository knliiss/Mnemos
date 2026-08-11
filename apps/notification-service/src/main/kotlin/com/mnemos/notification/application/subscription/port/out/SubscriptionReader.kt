package com.mnemos.notification.application.subscription.port.out

import com.mnemos.notification.application.common.pagination.PageResult
import com.mnemos.notification.application.subscription.model.SubscriptionView
import com.mnemos.notification.application.subscription.usecase.list.ListSubscriptionsQuery

interface SubscriptionReader {
    fun find(query: ListSubscriptionsQuery): PageResult<SubscriptionView>
}
