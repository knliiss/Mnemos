package com.mnemos.personalization.adapter.inbound.http.internal.profileappearance

import com.mnemos.personalization.application.ownership.model.ActiveProfileAppearanceSnapshot
import com.mnemos.personalization.contracts.internal.v1.InternalProfileAppearanceResponse

object InternalProfileAppearanceApiMapper {
    fun ActiveProfileAppearanceSnapshot.toResponse(): InternalProfileAppearanceResponse =
        InternalProfileAppearanceResponse(
            accountId = accountId.id,
            revision = revision,
            appearance = appearance,
            observedAt = observedAt,
        )
}
