package com.mnemos.event.adapter.out.persistence.localization

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "sao_localization_packs")
class SaoLocalizationPackEntity(
    @Id
    @Column(name = "locale", nullable = false, length = 16)
    var locale: String,
    @Column(name = "hash", nullable = false, length = 64)
    var hash: String,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "properties", nullable = false, columnDefinition = "jsonb")
    var properties: Map<String, String>,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
