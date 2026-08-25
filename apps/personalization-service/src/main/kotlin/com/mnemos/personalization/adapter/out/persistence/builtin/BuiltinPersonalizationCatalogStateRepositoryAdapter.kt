package com.mnemos.personalization.adapter.out.persistence.builtin

import com.mnemos.personalization.application.personalization.builtin.model.BuiltinPersonalizationCatalogState
import com.mnemos.personalization.application.personalization.builtin.port.out.BuiltinPersonalizationCatalogStateRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class BuiltinPersonalizationCatalogStateRepositoryAdapter(
    private val repository: BuiltinPersonalizationCatalogStateJpaRepository,
) : BuiltinPersonalizationCatalogStateRepository {
    override fun findForUpdate(): BuiltinPersonalizationCatalogState {
        val entity =
            requireNotNull(repository.findMainForUpdate()) {
                "Built-in personalization catalog state was not initialized"
            }

        return BuiltinPersonalizationCatalogState(
            revision = entity.revision,
            updatedAt = entity.updatedAt,
        )
    }

    override fun update(
        revision: Int,
        updatedAt: Instant,
    ) {
        val entity =
            requireNotNull(repository.findMainForUpdate()) {
                "Built-in personalization catalog state was not initialized"
            }

        entity.revision = revision
        entity.updatedAt = updatedAt
        repository.save(entity)
    }
}
