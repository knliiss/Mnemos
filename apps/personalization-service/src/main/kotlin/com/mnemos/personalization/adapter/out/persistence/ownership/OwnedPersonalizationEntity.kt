package com.mnemos.personalization.adapter.out.persistence.ownership

import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "owned_personalizations")
class OwnedPersonalizationEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 16)
    var ownerType: PersonalizationTarget,
    @Column(name = "owner_id", nullable = false)
    var ownerId: UUID,
    @Column(name = "personalization_id", nullable = false)
    var personalizationId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "personalization_type", nullable = false, length = 32)
    var personalizationType: PersonalizationType,
    @Column(name = "acquired_at", nullable = false)
    var acquiredAt: Instant,
    @Column(name = "active", nullable = false)
    var active: Boolean,
)
