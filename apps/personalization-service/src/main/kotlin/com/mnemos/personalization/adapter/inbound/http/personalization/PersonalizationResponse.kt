package com.mnemos.personalization.adapter.inbound.http.personalization

import com.mnemos.personalization.adapter.inbound.http.personalization.content.PersonalizationContentResponse
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.model.PersonalizationVisibility
import java.time.Instant
import java.util.UUID

data class PersonalizationResponse(
    val id: UUID,
    val creatorUserId: UUID?,
    val catalogKey: String?,
    val type: PersonalizationType,
    val origin: PersonalizationOrigin,
    val visibility: PersonalizationVisibility,
    val name: String,
    val content: PersonalizationContentResponse,
    val price: PersonalizationPriceResponse?,
    val status: PersonalizationStatus,
    val rejectionReason: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
