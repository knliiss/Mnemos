package com.mnemos.account.application.service.invite

import com.mnemos.account.application.context.account.ActiveAccountContext
import com.mnemos.account.application.context.account.ActiveAccountContextProvider
import com.mnemos.account.application.query.account.invite.AccountInviteListItem
import com.mnemos.account.application.query.account.invite.AccountInviteQueryRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission.MANAGE_MEMBERS
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
        val context =
            contextProvider.requirePermission(
                accountId = accountId,
                permission = MANAGE_MEMBERS,
            )

        return getInvites(context)
    }

    internal fun getInvites(context: ActiveAccountContext): List<AccountInviteListItem> =
        inviteQueryRepository.findAllByAccountId(
            accountId = context.account.id,
            now = Instant.now(),
        )
}
