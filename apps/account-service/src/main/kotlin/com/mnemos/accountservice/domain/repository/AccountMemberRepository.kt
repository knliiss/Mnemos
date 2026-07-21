package com.mnemos.accountservice.domain.repository

import com.mnemos.accountservice.domain.model.account.AccountMember
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId

interface AccountMemberRepository {
    fun create(member: AccountMember): AccountMember
    fun findById(memberId: AccountMemberId): AccountMember?
    fun findByAccountIdAndUserId(accountId: AccountId, userId: UserId): AccountMember?
    fun findAllByUserId(userId: UserId): List<AccountMember>
    fun existsByAccountIdAndUserId(accountId: AccountId, userId: UserId): Boolean
    fun deleteByAccountIdAndUserId(accountId: AccountId, userId: UserId)
    fun countByAccountId(accountId: AccountId): Int
}