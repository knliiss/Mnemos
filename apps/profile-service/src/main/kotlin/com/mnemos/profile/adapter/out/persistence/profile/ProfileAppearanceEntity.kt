package com.mnemos.profile.adapter.out.persistence.profile

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "profile_appearances")
class ProfileAppearanceEntity(
    @Id
    @Column(name = "account_id", nullable = false, updatable = false)
    var accountId: UUID,
    @Column(name = "revision", nullable = false)
    var revision: Long,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "appearance", nullable = false, columnDefinition = "jsonb")
    var appearance: ProfileAppearanceDocument.Appearance,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
