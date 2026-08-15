package com.mnemos.personalization.adapter.out.persistence.badge

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface BadgeJpaRepository : JpaRepository<BadgeEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT badge FROM BadgeEntity badge WHERE badge.id = :id")
    fun findByIdForUpdate(id: UUID): BadgeEntity?

    fun findByCode(code: String): BadgeEntity?
}
