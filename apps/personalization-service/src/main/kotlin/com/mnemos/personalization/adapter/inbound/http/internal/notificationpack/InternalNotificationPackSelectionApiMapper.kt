package com.mnemos.personalization.adapter.inbound.http.internal.notificationpack

import com.mnemos.personalization.application.ownership.event.NotificationPackContractMapper.toContract
import com.mnemos.personalization.application.ownership.model.ActiveNotificationPackSelection
import com.mnemos.personalization.contracts.internal.v1.InternalNotificationPackSelectionResponse

object InternalNotificationPackSelectionApiMapper {
    fun ActiveNotificationPackSelection.toResponse(): InternalNotificationPackSelectionResponse =
        InternalNotificationPackSelectionResponse(
            userId = userId.id,
            personalizationId = personalizationId.id,
            selectionRevision = selectionRevision,
            pack = content.toContract(),
        )
}
