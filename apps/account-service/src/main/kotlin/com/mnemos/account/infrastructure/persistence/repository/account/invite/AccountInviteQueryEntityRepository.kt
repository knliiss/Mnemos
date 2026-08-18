package com.mnemos.account.infrastructure.persistence.repository.account.invite

import com.mnemos.account.infrastructure.persistence.projection.account.invite.AccountInviteListProjection
import com.mnemos.account.persistence.entity.invite.AccountInviteEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.util.UUID

interface AccountInviteQueryEntityRepository : Repository<AccountInviteEntity, UUID> {
    @Query(
        """
        SELECT invite.id AS "id",
            invite.code AS "code",
            invite.created_by_user_id AS "createdByUserId",
            invite.created_at AS "createdAt",
            invite.expires_at AS "expiresAt",
            invite.consumed_at AS "consumedAt",
            invite.revoked_at AS "revokedAt"
        FROM account_invites invite
        WHERE invite.account_id = :accountId
        ORDER BY invite.created_at DESC, invite.id DESC
        """,
        nativeQuery = true,
    )
    fun findAllByAccountId(
        @Param("accountId") accountId: UUID,
    ): List<AccountInviteListProjection>
}
