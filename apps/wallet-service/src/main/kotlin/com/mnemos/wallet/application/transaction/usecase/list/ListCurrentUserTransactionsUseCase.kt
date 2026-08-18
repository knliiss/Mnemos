package com.mnemos.wallet.application.transaction.usecase.list

import com.mnemos.wallet.application.common.pagination.PageQuery
import com.mnemos.wallet.application.common.pagination.PageResult
import com.mnemos.wallet.application.transaction.model.TransactionView

fun interface ListCurrentUserTransactionsUseCase {
    fun execute(page: PageQuery): PageResult<TransactionView>
}
