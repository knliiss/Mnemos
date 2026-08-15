package com.mnemos.personalization.adapter.out.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.personalization.application.account.exception.ActiveAccountRequiredException
import com.mnemos.personalization.application.account.port.out.PersonalizationAccountAccess
import org.springframework.stereotype.Component

@Component
class PersonalizationAccountAccessAdapter(
    private val client: PersonalizationAccountInternalClient,
) : PersonalizationAccountAccess {
    override fun requireActiveAccount(userId: UserId): AccountId =
        client.resolve(userId = userId.id)
            ?.let { response -> AccountId(response.accountId) }
            ?: throw ActiveAccountRequiredException()

    override fun hasPermission(
        accountId: AccountId,
        userId: UserId,
        permission: RolePermission,
    ): Boolean =
        client
            .checkPermission(
                accountId = accountId.id,
                request =
                    CheckAccountPermissionRequest(
                        userId = userId.id,
                        permission = permission,
                    ),
            )
            .granted
}
