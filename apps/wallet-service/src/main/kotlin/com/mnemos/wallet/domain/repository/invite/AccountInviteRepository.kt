package com.mnemos.accountservice.domain.repository.invite

import com.mnemos.accountservice.domain.model.account.invite.AccountInvite
import com.mnemos.accountservice.domain.model.account.invite.InviteCode
import com.mnemos.accountservice.domain.model.account.invite.InviteId
import com.mnemos.common.core.model.id.AccountId

interface AccountInviteRepository {
    fun create(invite: AccountInvite): AccountInvite

    fun update(invite: AccountInvite): AccountInvite

    fun findByCode(code: InviteCode): AccountInvite?

    fun findByIdAndAccountIdForUpdate(
        inviteId: InviteId,
        accountId: AccountId,
    ): AccountInvite?

    fun findAllByAccountId(accountId: AccountId): List<AccountInvite>
}
