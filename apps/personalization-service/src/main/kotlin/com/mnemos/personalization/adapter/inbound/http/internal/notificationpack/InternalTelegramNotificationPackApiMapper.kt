package com.mnemos.personalization.adapter.inbound.http.internal.notificationpack

import com.mnemos.personalization.application.ownership.model.TelegramNotificationPackOption
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackSummaryResponse

object InternalTelegramNotificationPackApiMapper {
    fun TelegramNotificationPackOption.toResponse(): InternalTelegramNotificationPackSummaryResponse =
        InternalTelegramNotificationPackSummaryResponse(
            personalizationId = personalizationId.id,
            name = name,
            active = active,
        )
}
