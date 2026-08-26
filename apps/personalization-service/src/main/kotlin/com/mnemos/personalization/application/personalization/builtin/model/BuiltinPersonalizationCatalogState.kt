package com.mnemos.personalization.application.personalization.builtin.model

import java.time.Instant

data class BuiltinPersonalizationCatalogState(
    val revision: Int,
    val updatedAt: Instant,
)
