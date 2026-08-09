package com.mnemos.wallet.application.transaction

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.web.exception.common.AccountPermissionDeniedException
import com.mnemos.security.CurrentMnemosUser
import com.mnemos.wallet.api.dto.TransactionSummaryResponse
import com.mnemos.wallet.domain.repository.TransactionRepository
import com.mnemos.wallet.integration.account.client.AccountPermissionClient
import com.mnemos.wallet.integration.account.dto.CheckAccountPermissionRequest
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class TransactionHistoryQueryService(
    private val transactionRepository: TransactionRepository,
    private val currentMnemosUser: CurrentMnemosUser,
    private val accountPermissionClient: AccountPermissionClient,
) {
    companion object {
        const val PAGE_SIZE = 20
    }

    fun getHistory(
        accountId: AccountId,
        page: Int,
    ): List<TransactionSummaryResponse> {
        val userId = currentMnemosUser.requireUserId()
        val granted =
            accountPermissionClient
                .checkPermission(
                    accountId = accountId.id,
                    request =
                        CheckAccountPermissionRequest(
                            userId = userId,
                            permission = RolePermission.VIEW_TRANSACTION_HISTORY,
                        ),
                ).granted

        if (!granted) {
            throw AccountPermissionDeniedException(accountId)
        }

        return transactionRepository.findByAccountId(
            accountId,
            PageRequest.of(
                page,
                PAGE_SIZE,
            ),
        )
    }
}
