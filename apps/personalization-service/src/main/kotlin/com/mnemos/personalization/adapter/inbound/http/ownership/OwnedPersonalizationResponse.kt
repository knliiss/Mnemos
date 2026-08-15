package com.mnemos.personalization.adapter.inbound.http.ownership

import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import java.time.Instant
import java.util.UUID

data class OwnedPersonalizationResponse(
    val personalizationId: UUID,
    val ownerType: PersonalizationTarget,
    val ownerId: UUID,
    val type: PersonalizationType,
    val acquiredAt: Instant,
    val active: Boolean,
)
