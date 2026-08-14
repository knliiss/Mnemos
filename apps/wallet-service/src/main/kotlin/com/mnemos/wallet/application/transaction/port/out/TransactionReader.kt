package com.mnemos.wallet.application.transaction.port.out

import com.mnemos.wallet.application.common.pagination.PageQuery
import com.mnemos.wallet.application.common.pagination.PageResult
import com.mnemos.wallet.application.transaction.model.TransactionView
import com.mnemos.wallet.domain.wallet.model.WalletOwner

fun interface TransactionReader {
    fun findByOwner(
        owner: WalletOwner,
        page: PageQuery,
    ): PageResult<TransactionView>
}
