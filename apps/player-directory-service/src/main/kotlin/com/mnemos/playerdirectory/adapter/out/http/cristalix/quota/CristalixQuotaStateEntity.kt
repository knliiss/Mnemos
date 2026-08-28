package com.mnemos.playerdirectory.adapter.out.http.cristalix.quota

import com.mnemos.playerdirectory.adapter.out.http.cristalix.CristalixCredentialLane
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "cristalix_quota_states")
class CristalixQuotaStateEntity(
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "lane", nullable = false, length = 16)
    var lane: CristalixCredentialLane,
    @Column(name = "available_tokens", nullable = false)
    var availableTokens: Double,
    @Column(name = "last_refill_at", nullable = false)
    var lastRefillAt: Instant,
    @Column(name = "cooldown_until")
    var cooldownUntil: Instant?,
)
