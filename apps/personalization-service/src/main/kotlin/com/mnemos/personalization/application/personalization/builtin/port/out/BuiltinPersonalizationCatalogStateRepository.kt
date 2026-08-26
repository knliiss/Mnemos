package com.mnemos.personalization.application.personalization.builtin.port.out

import com.mnemos.personalization.application.personalization.builtin.model.BuiltinPersonalizationCatalogState
import java.time.Instant

interface BuiltinPersonalizationCatalogStateRepository {
    fun findForUpdate(): BuiltinPersonalizationCatalogState

    fun update(
        revision: Int,
        updatedAt: Instant,
    )
}
