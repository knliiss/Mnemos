package com.mnemos.wallet.application.transaction.usecase.list

import com.mnemos.wallet.application.common.pagination.PageQuery
import com.mnemos.wallet.application.common.pagination.PageResult
import com.mnemos.wallet.application.security.port.out.CurrentUserProvider
import com.mnemos.wallet.application.transaction.model.TransactionView
import com.mnemos.wallet.application.transaction.port.out.TransactionReader
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListCurrentUserTransactionsService(
    private val transactionReader: TransactionReader,
    private val currentUserProvider: CurrentUserProvider,
) : ListCurrentUserTransactionsUseCase {
    override fun execute(page: PageQuery): PageResult<TransactionView> {
        val userId = currentUserProvider.requireUserId()
        return transactionReader.findByOwner(
            owner = WalletOwner.User(userId),
            page = page,
        )
    }
}
