package com.mnemos.event.application.localization.service

import com.mnemos.event.application.localization.model.SaoLocalizationPack
import com.mnemos.event.application.localization.port.out.SaoLocalizationPackRepository
import org.springframework.stereotype.Service

@Service
class SaoLocalizationCatalogService(
    private val repository: SaoLocalizationPackRepository,
) {
    fun find(locale: String): SaoLocalizationPack? = repository.findByLocale(locale)

    fun list(): List<SaoLocalizationPack> = repository.findAll().sortedBy(SaoLocalizationPack::locale)
}
