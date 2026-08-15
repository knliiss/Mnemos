package com.mnemos.personalization.adapter.out.persistence.personalization

import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.model.PersonalizationVisibility
import com.mnemos.wallet.contracts.wallet.shared.Currency
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "personalizations")
class PersonalizationEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "creator_user_id", nullable = false)
    var creatorUserId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    var type: PersonalizationType,
    @Enumerated(EnumType.STRING)
    @Column(name = "origin", nullable = false, length = 16)
    var origin: PersonalizationOrigin,
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 16)
    var visibility: PersonalizationVisibility,
    @Column(name = "name", nullable = false, length = 64)
    var name: String,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", nullable = false, columnDefinition = "jsonb")
    var content: Map<String, Any?>,
    @Enumerated(EnumType.STRING)
    @Column(name = "price_currency", length = 16)
    var priceCurrency: Currency?,
    @Column(name = "price_amount")
    var priceAmount: Long?,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    var status: PersonalizationStatus,
    @Column(name = "rejection_reason", length = 500)
    var rejectionReason: String?,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
