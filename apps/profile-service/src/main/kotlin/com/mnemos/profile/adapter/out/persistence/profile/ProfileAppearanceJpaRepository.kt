package com.mnemos.profile.adapter.out.persistence.profile

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ProfileAppearanceJpaRepository : JpaRepository<ProfileAppearanceEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO profile_appearances (account_id, revision, appearance, updated_at)
            VALUES (
                :accountId,
                0,
                CAST(
                    '{"nickStyle":null,"profileTheme":null,"profileBackground":null,"avatarFrame":null,"profileEffect":null}'
                    AS jsonb
                ),
                TIMESTAMPTZ '1970-01-01 00:00:00+00'
            )
            ON CONFLICT (account_id) DO NOTHING
        """,
        nativeQuery = true,
    )
    fun ensureState(
        @Param("accountId") accountId: UUID,
    ): Int

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select appearance from ProfileAppearanceEntity appearance where appearance.accountId = :accountId")
    fun findForUpdate(
        @Param("accountId") accountId: UUID,
    ): ProfileAppearanceEntity?

    fun findAllByAccountIdIn(accountIds: Collection<UUID>): List<ProfileAppearanceEntity>
}
