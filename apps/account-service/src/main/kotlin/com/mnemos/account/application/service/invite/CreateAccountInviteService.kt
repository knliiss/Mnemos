package com.mnemos.account.application.service.invite

import com.mnemos.account.application.context.account.ActiveAccountContextProvider
import com.mnemos.account.application.port.invite.InviteCodeGenerator
import com.mnemos.account.domain.model.account.invite.AccountInvite
import com.mnemos.account.domain.model.account.invite.InviteCode
import com.mnemos.account.domain.model.account.invite.InviteId
import com.mnemos.account.domain.repository.invite.AccountInviteRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class CreateAccountInviteService(
    private val contextProvider: ActiveAccountContextProvider,
    private val inviteRepository: AccountInviteRepository,
    private val inviteCodeGenerator: InviteCodeGenerator,
) {
    @Transactional
    fun createInvite(
        accountId: AccountId,
        expiresIn: Duration?,
    ): CreateAccountInviteResult {
        require(expiresIn == null || (!expiresIn.isZero && !expiresIn.isNegative)) {
            "Invite expiration duration must be positive"
        }

        val context =
            contextProvider.requirePermissionForUpdate(
                accountId = accountId,
                permission = RolePermission.MANAGE_MEMBERS,
            )

        val now = Instant.now()

        val invite =
            AccountInvite.create(
                accountId = accountId,
                code = inviteCodeGenerator.generate(),
                createdByUserId = context.user.id,
                expiresAt = expiresIn?.let(now::plus),
                now = now,
            )

        val createdInvite = inviteRepository.create(invite)

        return CreateAccountInviteResult(
            id = createdInvite.id,
            code = createdInvite.code,
            createdAt = createdInvite.createdAt,
            expiresAt = createdInvite.expiresAt,
        )
    }

    data class CreateAccountInviteResult(
        val id: InviteId,
        val code: InviteCode,
        val createdAt: Instant,
        val expiresAt: Instant?,
    )
}
