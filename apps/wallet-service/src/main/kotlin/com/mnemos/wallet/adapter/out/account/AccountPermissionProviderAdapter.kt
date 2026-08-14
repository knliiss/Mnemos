package com.mnemos.wallet.adapter.out.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.wallet.adapter.out.account.dto.CheckAccountPermissionRequest
import com.mnemos.wallet.application.account.port.out.AccountPermissionProvider
import org.springframework.stereotype.Component

@Component
class AccountPermissionProviderAdapter(
    private val accountPermissionClient: AccountPermissionClient,
) : AccountPermissionProvider {
    override fun hasPermission(
        accountId: AccountId,
        userId: UserId,
        permission: RolePermission,
    ): Boolean =
        accountPermissionClient
            .checkPermission(
                accountId = accountId.id,
                request =
                    CheckAccountPermissionRequest(
                        userId = userId.id,
                        permission = permission,
                    ),
            ).granted
}
