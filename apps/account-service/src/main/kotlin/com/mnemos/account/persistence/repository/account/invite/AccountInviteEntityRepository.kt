package com.mnemos.account.persistence.repository.account.invite

import com.mnemos.account.persistence.entity.invite.AccountInviteEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface AccountInviteEntityRepository : JpaRepository<AccountInviteEntity, UUID> {
    fun findByCode(code: String): AccountInviteEntity?

    fun findAllByAccountIdOrderByCreatedAtDesc(accountId: UUID): List<AccountInviteEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT invite
        FROM AccountInviteEntity invite
        WHERE invite.id = :inviteId
          AND invite.accountId = :accountId
        """,
    )
    fun findByIdAndAccountIdForUpdate(
        @Param("inviteId") inviteId: UUID,
        @Param("accountId") accountId: UUID,
    ): AccountInviteEntity?
}
