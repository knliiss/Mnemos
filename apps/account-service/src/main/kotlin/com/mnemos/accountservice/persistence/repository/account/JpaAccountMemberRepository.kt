package com.mnemos.accountservice.persistence.repository.account

import com.mnemos.accountservice.persistence.entity.AccountMemberEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaAccountMemberRepository : JpaRepository<AccountMemberEntity, UUID> {
    fun findByAccountIdAndUserId(accountId: UUID, userId: UUID): AccountMemberEntity?
    fun findAllByUserId(userId: UUID): List<AccountMemberEntity>
    fun existsByAccountIdAndUserId(accountId: UUID, userId: UUID): Boolean
    fun deleteByAccountIdAndUserId(accountId: UUID, userId: UUID)
    fun countByAccountId(accountId: UUID): Long
}