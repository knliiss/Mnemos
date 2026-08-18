package com.mnemos.profile.adapter.out.persistence.profile

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ProfileAppearanceJpaRepository : JpaRepository<ProfileAppearanceEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select appearance from ProfileAppearanceEntity appearance where appearance.accountId = :accountId")
    fun findForUpdate(
        @Param("accountId") accountId: UUID,
    ): ProfileAppearanceEntity?

    fun findAllByAccountIdIn(accountIds: Collection<UUID>): List<ProfileAppearanceEntity>
}
