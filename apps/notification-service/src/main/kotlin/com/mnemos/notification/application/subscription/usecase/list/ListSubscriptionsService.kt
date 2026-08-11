package com.mnemos.notification.application.subscription.usecase.list

import com.mnemos.notification.application.common.pagination.PageResult
import com.mnemos.notification.application.subscription.model.SubscriptionView
import com.mnemos.notification.application.subscription.port.out.SubscriptionReader
import org.springframework.stereotype.Service

@Service
class ListSubscriptionsService(
    private val subscriptionReader: SubscriptionReader,
) : ListSubscriptionsUseCase {
    override fun execute(query: ListSubscriptionsQuery): PageResult<SubscriptionView> = subscriptionReader.find(query)
}
