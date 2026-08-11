package com.mnemos.notification.application.subscription.usecase.list

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.common.pagination.PageQuery

data class ListSubscriptionsQuery(
    val userId: UserId,
    val page: PageQuery,
)
