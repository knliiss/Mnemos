package com.mnemos.event.application.localization.port.out

import com.mnemos.event.application.localization.model.SaoLocalizationPack

interface SaoLocalizationPackRepository {
    fun findByLocale(locale: String): SaoLocalizationPack?

    fun findAll(): List<SaoLocalizationPack>

    fun save(pack: SaoLocalizationPack): SaoLocalizationPack
}
