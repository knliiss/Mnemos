package com.mnemos.notification.adapter.out.persistence.subscription.query

import com.mnemos.notification.adapter.out.persistence.common.pagination.PagePersistenceMapper.toPageResult
import com.mnemos.notification.adapter.out.persistence.subscription.SubscriptionJpaRepository
import com.mnemos.notification.adapter.out.persistence.subscription.query.SubscriptionQueryMapper.toView
import com.mnemos.notification.application.common.pagination.PageResult
import com.mnemos.notification.application.subscription.model.SubscriptionView
import com.mnemos.notification.application.subscription.port.out.SubscriptionReader
import com.mnemos.notification.application.subscription.usecase.list.ListSubscriptionsQuery
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class SubscriptionQueryJpaAdapter(
    private val subscriptionJpaRepository: SubscriptionJpaRepository,
) : SubscriptionReader {
    override fun find(query: ListSubscriptionsQuery): PageResult<SubscriptionView> {
        val page =
            subscriptionJpaRepository.findViews(
                userId = query.userId.id,
                pageable = PageRequest.of(query.page.page, query.page.size),
            )

        return page.toPageResult { it.toView() }
    }
}
