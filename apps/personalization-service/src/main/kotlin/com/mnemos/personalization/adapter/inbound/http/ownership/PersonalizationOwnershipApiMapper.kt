package com.mnemos.personalization.adapter.inbound.http.ownership

import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization

object PersonalizationOwnershipApiMapper {
    fun OwnedPersonalization.toResponse(): OwnedPersonalizationResponse =
        OwnedPersonalizationResponse(
            personalizationId = personalizationId.id,
            ownerType = owner.target,
            ownerId = owner.id,
            type = type,
            acquiredAt = acquiredAt,
            active = active,
        )
}
