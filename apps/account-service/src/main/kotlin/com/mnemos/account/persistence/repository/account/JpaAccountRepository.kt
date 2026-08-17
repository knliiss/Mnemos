package com.mnemos.account.persistence.repository.account

import com.mnemos.account.persistence.entity.account.AccountEntity
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface JpaAccountRepository : JpaRepository<AccountEntity, UUID> {
    fun existsByCristalixUuid(cristalixUuid: UUID): Boolean

    fun findAllByOrderByIdAsc(pageable: Pageable): List<AccountEntity>

    fun findByIdGreaterThanOrderByIdAsc(
        id: UUID,
        pageable: Pageable,
    ): List<AccountEntity>

    @Query(
        """
            SELECT account
            FROM AccountEntity account
            WHERE account.status =
                com.mnemos.account.domain.model.account.AccountStatus.ACTIVE
              AND EXISTS (
                  SELECT member.id
                  FROM AccountMemberEntity member
                  WHERE member.accountId = account.id
                    AND member.userId = :userId
              )
            ORDER BY account.createdAt ASC
        """,
    )
    fun findAllAvailableForUser(
        @Param("userId") userId: UUID,
    ): List<AccountEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
            SELECT account
            FROM AccountEntity account
            WHERE account.id = :id
        """,
    )
    fun findByIdForUpdate(
        @Param("id") id: UUID,
    ): AccountEntity?

    @Query(
        """
            SELECT account
            FROM AccountEntity account
            WHERE account.status = com.mnemos.account.domain.model.account.AccountStatus.ACTIVE
              AND LOWER(account.cristalixNickname) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY account.cristalixNickname ASC
        """,
    )
    fun searchActiveAccounts(
        @Param("query") query: String,
    ): List<AccountEntity>

    @Query(
        """
            SELECT account
            FROM AccountEntity account
            WHERE account.id = :accountId
              AND account.status = com.mnemos.account.domain.model.account.AccountStatus.ACTIVE
        """,
    )
    fun findActiveById(accountId: UUID): AccountEntity?

    @Query(
        """
            SELECT account
            FROM AccountEntity account
            WHERE account.status = com.mnemos.account.domain.model.account.AccountStatus.ACTIVE
              AND LOWER(account.cristalixNickname) = LOWER(:nickname)
        """,
    )
    fun findActiveByCristalixNickname(
        @Param("nickname") nickname: String,
    ): AccountEntity?

    @Query(
        """
            SELECT account
            FROM AccountEntity account
            WHERE account.status = com.mnemos.account.domain.model.account.AccountStatus.ACTIVE
              AND EXISTS (
                  SELECT user.id
                  FROM UserEntity user
                  WHERE user.id = :userId
                    AND user.activeAccountId = account.id
              )
        """,
    )
    fun findActiveByUserId(
        @Param("userId") userId: UUID,
    ): AccountEntity?
}
