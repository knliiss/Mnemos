package com.mnemos.accountservice.application.service.invite

import com.mnemos.accountservice.application.context.account.ActiveAccountContextProvider
import com.mnemos.accountservice.domain.exception.invite.AccountInviteNotFoundException
import com.mnemos.accountservice.domain.model.account.invite.InviteId
import com.mnemos.accountservice.domain.repository.invite.AccountInviteRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission.MANAGE_ACCOUNT_INVITES
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class RevokeAccountInviteService(
    private val contextProvider: ActiveAccountContextProvider,
    private val inviteRepository: AccountInviteRepository,
) {
    @Transactional
    fun revokeInvite(
        accountId: AccountId,
        inviteId: InviteId,
    ) {
        contextProvider.requirePermissionForUpdate(
            accountId = accountId,
            permission = MANAGE_ACCOUNT_INVITES,
        )

        val invite =
            inviteRepository.findByIdAndAccountIdForUpdate(
                inviteId = inviteId,
                accountId = accountId,
            ) ?: throw AccountInviteNotFoundException(
                inviteId = inviteId,
                accountId = accountId,
            )

        if (invite.revoke(Instant.now())) {
            inviteRepository.update(invite)
        }
    }
}
