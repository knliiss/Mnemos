package com.mnemos.personalization.adapter.out.persistence.badge

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BadgeJpaRepository : JpaRepository<BadgeEntity, UUID> {
    fun findByCode(code: String): BadgeEntity?
}
