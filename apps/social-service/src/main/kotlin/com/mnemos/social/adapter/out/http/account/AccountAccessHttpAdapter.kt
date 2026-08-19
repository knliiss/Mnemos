package com.mnemos.social.adapter.out.http.account

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.social.application.actor.exception.ActiveSocialAccountRequiredException
import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.actor.port.out.AccountAccessPort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccountAccessHttpAdapter(
    private val client: AccountServiceClient,
) : AccountAccessPort {
    override fun resolveActiveActor(userId: UUID): SocialActor {
        val account = client.resolveActiveAccount(userId)
            ?: throw ActiveSocialAccountRequiredException()

        return SocialActor(
            userId = userId,
            accountId = account.accountId,
            cristalixNickname = account.cristalixNickname,
        )
    }

    override fun isActiveAccount(accountId: UUID): Boolean = client.findActiveAccount(accountId) != null

    override fun hasPermission(
        accountId: UUID,
        userId: UUID,
        permission: RolePermission,
    ): Boolean =
        client
            .checkPermission(
                accountId = accountId,
                request =
                    CheckAccountPermissionRequest(
                        userId = userId,
                        permission = permission,
                    ),
            ).granted
}
