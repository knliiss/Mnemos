package com.mnemos.personalization.adapter.out.persistence.builtin

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface BuiltinPersonalizationCatalogStateJpaRepository : JpaRepository<BuiltinPersonalizationCatalogStateEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT state
        FROM BuiltinPersonalizationCatalogStateEntity state
        WHERE state.id = 'main'
        """,
    )
    fun findMainForUpdate(): BuiltinPersonalizationCatalogStateEntity?
}
