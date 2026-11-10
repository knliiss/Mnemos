package com.mnemos.wallet.application.transaction.usecase.list

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.web.exception.common.AccountPermissionDeniedException
import com.mnemos.wallet.application.account.port.out.AccountPermissionProvider
import com.mnemos.wallet.application.account.port.out.TelegramWalletContextProvider
import com.mnemos.wallet.application.common.pagination.PageQuery
import com.mnemos.wallet.application.transaction.port.out.TransactionReader
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetTelegramAccountTransactionsService(
    private val contexts: TelegramWalletContextProvider,
    private val permissions: AccountPermissionProvider,
    private val transactions: TransactionReader,
) {
    fun execute(
        telegramId: Long,
        page: PageQuery,
    ): TelegramAccountTransactionSnapshot? {
        val context = contexts.findByTelegramId(telegramId) ?: return null
        val accountId = context.activeAccountId ?: return null

        if (!permissions.hasPermission(accountId, context.userId, RolePermission.VIEW_TRANSACTION_HISTORY)) {
            throw AccountPermissionDeniedException(
                accountId = accountId,
                requiredPermissions = setOf(RolePermission.VIEW_TRANSACTION_HISTORY.name),
            )
        }

        return TelegramAccountTransactionSnapshot(
            accountId = accountId,
            page = transactions.findByOwner(owner = WalletOwner.Account(accountId), page = page),
        )
    }
}
