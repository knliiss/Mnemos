package com.mnemos.accountservice.persistence.repository.account.invite

import com.mnemos.accountservice.domain.model.account.invite.AccountInvite
import com.mnemos.accountservice.domain.model.account.invite.InviteCode
import com.mnemos.accountservice.domain.model.account.invite.InviteId
import com.mnemos.accountservice.domain.repository.invite.AccountInviteRepository
import com.mnemos.accountservice.persistence.mapper.AccountInvitePersistenceMapper.toDomain
import com.mnemos.accountservice.persistence.mapper.AccountInvitePersistenceMapper.toEntity
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Repository

@Repository
class AccountInviteRepositoryAdapter(
    private val repository: AccountInviteEntityRepository,
) : AccountInviteRepository {
    override fun create(invite: AccountInvite): AccountInvite = repository.save(invite.toEntity()).toDomain()

    override fun update(invite: AccountInvite): AccountInvite = repository.save(invite.toEntity()).toDomain()

    override fun findByCode(code: InviteCode): AccountInvite? =
        repository
            .findByCode(code.value)
            ?.toDomain()

    override fun findByIdAndAccountIdForUpdate(
        inviteId: InviteId,
        accountId: AccountId,
    ): AccountInvite? =
        repository
            .findByIdAndAccountIdForUpdate(
                inviteId = inviteId.value,
                accountId = accountId.value,
            )?.toDomain()

    override fun findAllByAccountId(accountId: AccountId): List<AccountInvite> =
        repository
            .findAllByAccountIdOrderByCreatedAtDesc(accountId.value)
            .map { it.toDomain() }
}
