package com.mnemos.social.adapter.inbound.http.spotlight

import com.mnemos.social.application.spotlight.usecase.create.CreateDropSpotlightCommand
import com.mnemos.social.domain.spotlight.model.DropSpotlight

object DropSpotlightApiMapper {
    fun CreateDropSpotlightRequest.toCommand(): CreateDropSpotlightCommand =
        CreateDropSpotlightCommand(
            itemDropEventId = itemDropEventId,
            idempotencyKey = idempotencyKey,
        )

    fun DropSpotlight.toResponse(): DropSpotlightResponse =
        DropSpotlightResponse(
            id = id.id,
            itemDropEventId = itemDrop.id,
            sourceAccountId = sourceAccountId,
            pricePrime = pricePrime,
            scheduledAt = scheduledAt,
            status = status.name,
        )
}
