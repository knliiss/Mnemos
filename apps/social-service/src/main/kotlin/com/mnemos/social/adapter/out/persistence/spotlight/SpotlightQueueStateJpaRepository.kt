package com.mnemos.social.adapter.out.persistence.spotlight

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface SpotlightQueueStateJpaRepository : JpaRepository<SpotlightQueueStateEntity, Short> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select state from SpotlightQueueStateEntity state where state.id = 1")
    fun findForUpdate(): SpotlightQueueStateEntity
}
