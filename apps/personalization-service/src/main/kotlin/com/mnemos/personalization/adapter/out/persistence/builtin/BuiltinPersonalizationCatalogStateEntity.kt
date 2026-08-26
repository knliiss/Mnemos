package com.mnemos.personalization.adapter.out.persistence.builtin

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "builtin_personalization_catalog_state")
class BuiltinPersonalizationCatalogStateEntity(
    @Id
    @Column(name = "id", nullable = false, length = 16)
    var id: String,
    @Column(name = "revision", nullable = false)
    var revision: Int,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
