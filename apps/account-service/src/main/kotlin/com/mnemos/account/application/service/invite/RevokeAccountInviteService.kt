package com.mnemos.account.application.service.invite

import com.mnemos.account.application.context.account.ActiveAccountContextProvider
import com.mnemos.account.domain.exception.invite.AccountInviteNotFoundException
import com.mnemos.account.domain.model.account.invite.InviteId
import com.mnemos.account.domain.repository.invite.AccountInviteRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission.MANAGE_MEMBERS
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
            permission = MANAGE_MEMBERS,
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
