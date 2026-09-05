package com.mnemos.event.adapter.out.persistence.localization

import com.mnemos.event.application.localization.model.SaoLocalizationPack

object SaoLocalizationPackPersistenceMapper {
    fun SaoLocalizationPackEntity.toModel(): SaoLocalizationPack =
        SaoLocalizationPack(
            locale = locale,
            hash = hash,
            properties = properties,
            updatedAt = updatedAt,
        )

    fun SaoLocalizationPack.toEntity(): SaoLocalizationPackEntity =
        SaoLocalizationPackEntity(
            locale = locale,
            hash = hash,
            properties = properties,
            updatedAt = updatedAt,
        )
}
