package com.mnemos.moderation.adapter.out.persistence.sanction

import com.mnemos.moderation.domain.sanction.model.DomainSanctionStatus
import com.mnemos.moderation.domain.sanction.model.DomainSanctionType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "sanctions")
class SanctionEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    var userId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    var type: DomainSanctionType,
    @Column(name = "reason", nullable = false, updatable = false)
    var reason: String,
    @Column(name = "internal_note")
    var internalNote: String?,
    @Column(name = "created_by_user_id", nullable = false, updatable = false)
    var createdByUserId: UUID,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant,
    @Column(name = "expires_at")
    var expiresAt: Instant?,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: DomainSanctionStatus,
)
