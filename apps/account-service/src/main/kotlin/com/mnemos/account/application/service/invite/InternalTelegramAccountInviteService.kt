package com.mnemos.account.application.service.invite

import com.mnemos.account.application.context.account.InternalTelegramAccountContextProvider
import com.mnemos.account.application.query.account.invite.AccountInviteListItem
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.invite.InviteId
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.permission.RolePermission.MANAGE_MEMBERS
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class InternalTelegramAccountInviteService(
    private val contextProvider: InternalTelegramAccountContextProvider,
    private val createInviteService: CreateAccountInviteService,
    private val inviteQueryService: AccountInviteQueryService,
    private val revokeInviteService: RevokeAccountInviteService,
) {
    @Transactional(readOnly = true)
    fun getInvites(telegramId: TelegramId): InternalTelegramAccountInvites? {
        val context =
            contextProvider.requirePermission(
                telegramId = telegramId,
                permission = MANAGE_MEMBERS,
            ) ?: return null

        return InternalTelegramAccountInvites(
            accountId = context.account.id,
            cristalixNickname = context.account.cristalixNickname,
            invites = inviteQueryService.getInvites(context),
        )
    }

    @Transactional
    fun createInvite(
        telegramId: TelegramId,
        expiresInMinutes: Long?,
    ): CreateAccountInviteService.CreateAccountInviteResult? {
        require(expiresInMinutes == null || expiresInMinutes in 1..MAX_INVITE_EXPIRY_MINUTES) {
            "Invite expiration must be between 1 and $MAX_INVITE_EXPIRY_MINUTES minutes"
        }

        val context =
            contextProvider.requirePermissionForUpdate(
                telegramId = telegramId,
                permission = MANAGE_MEMBERS,
            ) ?: return null

        return createInviteService.createInvite(
            context = context,
            expiresIn = expiresInMinutes?.let(Duration::ofMinutes),
        )
    }

    @Transactional
    fun revokeInvite(
        telegramId: TelegramId,
        inviteId: InviteId,
    ): Boolean? {
        val context =
            contextProvider.requirePermissionForUpdate(
                telegramId = telegramId,
                permission = MANAGE_MEMBERS,
            ) ?: return null

        revokeInviteService.revokeInvite(
            context = context,
            inviteId = inviteId,
        )

        return true
    }

    private companion object {
        const val MAX_INVITE_EXPIRY_MINUTES = 10_080L
    }
}

data class InternalTelegramAccountInvites(
    val accountId: AccountId,
    val cristalixNickname: CristalixNickname,
    val invites: List<AccountInviteListItem>,
)
