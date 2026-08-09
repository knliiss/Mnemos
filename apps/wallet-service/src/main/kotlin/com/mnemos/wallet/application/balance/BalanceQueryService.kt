package com.mnemos.wallet.application.balance

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.web.exception.common.AccountPermissionDeniedException
import com.mnemos.security.CurrentMnemosUser
import com.mnemos.wallet.api.dto.BalanceResponse
import com.mnemos.wallet.domain.repository.BalanceRepository
import com.mnemos.wallet.integration.account.client.AccountPermissionClient
import com.mnemos.wallet.integration.account.dto.CheckAccountPermissionRequest
import org.springframework.stereotype.Service

@Service
class BalanceQueryService(
    private val balanceRepository: BalanceRepository,
    private val currentMnemosUser: CurrentMnemosUser,
    private val accountPermissionClient: AccountPermissionClient,
) {
    fun getBalance(accountId: AccountId): List<BalanceResponse> {
        val userId = currentMnemosUser.requireUserId()
        val granted =
            accountPermissionClient
                .checkPermission(
                    accountId = accountId.id,
                    request =
                        CheckAccountPermissionRequest(
                            userId = userId,
                            permission = RolePermission.VIEW_WALLET,
                        ),
                ).granted

        if (!granted) {
            throw AccountPermissionDeniedException(accountId)
        }

        return balanceRepository.findAllByAccountId(accountId)
    }
}
