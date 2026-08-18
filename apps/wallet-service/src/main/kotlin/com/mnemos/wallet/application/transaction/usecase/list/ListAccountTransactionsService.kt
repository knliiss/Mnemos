package com.mnemos.wallet.application.transaction.usecase.list

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.web.exception.common.AccountPermissionDeniedException
import com.mnemos.wallet.application.account.port.out.AccountPermissionProvider
import com.mnemos.wallet.application.common.pagination.PageResult
import com.mnemos.wallet.application.security.port.out.CurrentUserProvider
import com.mnemos.wallet.application.transaction.model.TransactionView
import com.mnemos.wallet.application.transaction.port.out.TransactionReader
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListAccountTransactionsService(
    private val transactionReader: TransactionReader,
    private val currentUserProvider: CurrentUserProvider,
    private val accountPermissionProvider: AccountPermissionProvider,
) : ListAccountTransactionsUseCase {
    override fun execute(query: ListAccountTransactionsQuery): PageResult<TransactionView> {
        val userId = currentUserProvider.requireUserId()
        val granted =
            accountPermissionProvider.hasPermission(
                accountId = query.accountId,
                userId = userId,
                permission = RolePermission.VIEW_TRANSACTION_HISTORY,
            )

        if (!granted) {
            throw AccountPermissionDeniedException(query.accountId)
        }

        return transactionReader.findByOwner(
            owner = WalletOwner.Account(query.accountId),
            page = query.page,
            sort = query.sort,
        )
    }
}
