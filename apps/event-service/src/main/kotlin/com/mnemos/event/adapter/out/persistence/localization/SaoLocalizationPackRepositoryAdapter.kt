package com.mnemos.event.adapter.out.persistence.localization

import com.mnemos.event.adapter.out.persistence.localization.SaoLocalizationPackPersistenceMapper.toEntity
import com.mnemos.event.adapter.out.persistence.localization.SaoLocalizationPackPersistenceMapper.toModel
import com.mnemos.event.application.localization.model.SaoLocalizationPack
import com.mnemos.event.application.localization.port.out.SaoLocalizationPackRepository
import org.springframework.stereotype.Repository

@Repository
class SaoLocalizationPackRepositoryAdapter(
    private val repository: SaoLocalizationPackJpaRepository,
) : SaoLocalizationPackRepository {
    override fun findByLocale(locale: String): SaoLocalizationPack? =
        repository
            .findById(locale)
            .orElse(null)
            ?.toModel()

    override fun findAll(): List<SaoLocalizationPack> =
        repository.findAll().map { entity -> entity.toModel() }

    override fun save(pack: SaoLocalizationPack): SaoLocalizationPack =
        repository.save(pack.toEntity()).toModel()
}
