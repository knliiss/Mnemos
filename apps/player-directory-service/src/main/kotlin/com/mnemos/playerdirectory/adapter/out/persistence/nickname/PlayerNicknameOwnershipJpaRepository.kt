package com.mnemos.playerdirectory.adapter.out.persistence.nickname

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PlayerNicknameOwnershipJpaRepository : JpaRepository<PlayerNicknameOwnershipEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT ownership
        FROM PlayerNicknameOwnershipEntity ownership
        WHERE ownership.normalizedNickname IN :normalizedNicknames
        ORDER BY ownership.normalizedNickname
        """,
    )
    fun findByNormalizedNicknamesForUpdate(
        @Param("normalizedNicknames") normalizedNicknames: Set<String>,
    ): List<PlayerNicknameOwnershipEntity>
}
