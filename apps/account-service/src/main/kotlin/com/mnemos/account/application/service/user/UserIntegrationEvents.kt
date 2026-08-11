package com.mnemos.account.application.service.user

import com.mnemos.account.domain.model.user.User
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.contracts.account.v1.UserCreatedV1
import com.mnemos.messaging.core.event.IntegrationEvent

object UserIntegrationEvents {
    fun User.created(): IntegrationEvent<UserCreatedV1> =
        IntegrationEvent(
            type = AccountEventType.USER_CREATED,
            aggregateId = id.id,
            occurredAt = createdAt,
            payload =
                UserCreatedV1(
                    telegramId = telegramId.id,
                ),
        )
}
