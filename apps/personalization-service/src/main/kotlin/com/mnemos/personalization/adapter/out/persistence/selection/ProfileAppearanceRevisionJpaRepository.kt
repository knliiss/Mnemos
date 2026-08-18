package com.mnemos.personalization.adapter.out.persistence.selection

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ProfileAppearanceRevisionJpaRepository : JpaRepository<ProfileAppearanceRevisionEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO profile_appearance_revisions (account_id, revision)
            VALUES (:accountId, 0)
            ON CONFLICT (account_id) DO NOTHING
        """,
        nativeQuery = true,
    )
    fun ensureState(accountId: UUID): Int

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "SELECT state FROM ProfileAppearanceRevisionEntity state " +
            "WHERE state.accountId = :accountId",
    )
    fun findByAccountIdForUpdate(accountId: UUID): ProfileAppearanceRevisionEntity?
}
