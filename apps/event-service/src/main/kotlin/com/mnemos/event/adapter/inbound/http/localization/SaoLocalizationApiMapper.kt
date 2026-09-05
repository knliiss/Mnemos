package com.mnemos.event.adapter.inbound.http.localization

import com.mnemos.event.application.localization.model.SaoLocalizationPack

object SaoLocalizationApiMapper {
    fun SaoLocalizationPack.toResponse(): SaoLocalizationPackResponse =
        SaoLocalizationPackResponse(
            locale = locale,
            hash = hash,
            properties = properties,
            updatedAt = updatedAt,
        )

    fun SaoLocalizationPack.toSummaryResponse(): SaoLocalizationPackSummaryResponse =
        SaoLocalizationPackSummaryResponse(
            locale = locale,
            hash = hash,
            updatedAt = updatedAt,
        )
}
