package com.mnemos.accountservice.persistence.repository.account

import com.mnemos.accountservice.domain.model.account.AccountMember
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.repository.AccountMemberRepository
import com.mnemos.accountservice.persistence.mapper.AccountMemberPersistenceMapper.toDomain
import com.mnemos.accountservice.persistence.mapper.AccountMemberPersistenceMapper.toEntity
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId
import org.springframework.stereotype.Repository

@Repository
class AccountMemberRepositoryAdapter(
    private val jpaAccountMemberRepository: JpaAccountMemberRepository
) : AccountMemberRepository {

    override fun create(member: AccountMember): AccountMember {
        return jpaAccountMemberRepository.save(member.toEntity()).toDomain()
    }

    override fun findById(memberId: AccountMemberId): AccountMember? {
        return jpaAccountMemberRepository.findById(memberId.value)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByAccountIdAndUserId(
        accountId: AccountId,
        userId: UserId
    ): AccountMember? {
        return jpaAccountMemberRepository.findByAccountIdAndUserId(
           accountId = accountId.value,
            userId = userId.value
        )?.toDomain()
    }

    override fun findAllByUserId(userId: UserId): List<AccountMember> {
        return jpaAccountMemberRepository.findAllByUserId(userId.value)
            .map { it.toDomain() }
    }

    override fun existsByAccountIdAndUserId(
        accountId: AccountId,
        userId: UserId
    ): Boolean {
        return jpaAccountMemberRepository.existsByAccountIdAndUserId(
            accountId = accountId.value,
            userId = userId.value
        )
    }

    override fun deleteByAccountIdAndUserId(
        accountId: AccountId,
        userId: UserId
    ) {
        jpaAccountMemberRepository.deleteByAccountIdAndUserId(
            accountId = accountId.value,
            userId = userId.value
        )
    }

    override fun countByAccountId(accountId: AccountId): Int {
        return jpaAccountMemberRepository.countByAccountId(accountId = accountId.value).toInt()
    }
}