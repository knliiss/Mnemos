package com.mnemos.personalization.adapter.inbound.http.ownership

import com.mnemos.personalization.adapter.inbound.http.common.PageResponse
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization

object PersonalizationOwnershipApiMapper {
    fun PageResult<OwnedPersonalization>.toResponse(): PageResponse<OwnedPersonalizationResponse> =
        PageResponse(
            items = items.map { ownership -> ownership.toResponse() },
            page = page,
            size = size,
            totalItems = totalItems,
            totalPages = totalPages,
            hasNextPage = hasNextPage,
        )

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
