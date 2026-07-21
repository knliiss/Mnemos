package com.mnemos.accountservice.persistence.repository.account

import com.mnemos.accountservice.persistence.entity.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface JpaAccountRepository : JpaRepository<AccountEntity, UUID> {
    fun findByCristalixUuid(cristalixUuid: UUID): AccountEntity?
    fun existsByCristalixUuid(cristalixUuid: UUID): Boolean

    @Query(
        """
            SELECT a
            FROM AccountMemberEntity am
            JOIN AccountEntity a ON a.id = am.accountId
            WHERE am.userId = :userId
        """
    )
    fun findAllAvailableForUser(userId: UUID): List<AccountEntity>
    fun countById(id: UUID): Long
}