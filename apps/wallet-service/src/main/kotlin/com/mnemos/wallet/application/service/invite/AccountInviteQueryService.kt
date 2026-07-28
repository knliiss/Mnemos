package com.mnemos.accountservice.application.service.invite

import com.mnemos.accountservice.application.context.account.ActiveAccountContextProvider
import com.mnemos.accountservice.application.query.account.invite.AccountInviteListItem
import com.mnemos.accountservice.application.query.account.invite.AccountInviteQueryRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission.MANAGE_ACCOUNT_INVITES
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional(readOnly = true)
class AccountInviteQueryService(
    private val contextProvider: ActiveAccountContextProvider,
    private val inviteQueryRepository: AccountInviteQueryRepository,
) {
    fun getInvites(accountId: AccountId): List<AccountInviteListItem> {
        contextProvider.requirePermission(
            accountId = accountId,
            permission = MANAGE_ACCOUNT_INVITES,
        )

        return inviteQueryRepository.findAllByAccountId(
            accountId = accountId,
            now = Instant.now(),
        )
    }
}
