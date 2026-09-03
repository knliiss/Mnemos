package com.mnemos.wallet.application.transaction.usecase.list

import com.mnemos.wallet.application.common.pagination.PageQuery
import com.mnemos.wallet.application.common.pagination.PageResult
import com.mnemos.wallet.application.transaction.model.TransactionView
import com.mnemos.wallet.application.transaction.port.out.TransactionReader
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListAdminAccountTransactionsService(
    private val transactionReader: TransactionReader,
) {
    fun execute(
        accountId: AccountId,
        page: PageQuery,
    ): PageResult<TransactionView> =
        transactionReader.findByOwner(
            owner = WalletOwner.Account(accountId),
            page = page,
        )
}
