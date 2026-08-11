package com.mnemos.notification.domain.subscription.exception

import com.mnemos.common.web.exception.AppException
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import org.springframework.http.HttpStatus

class SubscriptionNotFoundException(
    subscriptionId: SubscriptionId,
) : AppException(
        message = "Subscription not found",
        code = "SUBSCRIPTION_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details = mapOf("subscriptionId" to subscriptionId.id),
    )
