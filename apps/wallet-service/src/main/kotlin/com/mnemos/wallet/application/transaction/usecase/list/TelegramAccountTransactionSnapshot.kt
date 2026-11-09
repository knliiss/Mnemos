package com.mnemos.wallet.application.transaction.usecase.list

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.application.common.pagination.PageResult
import com.mnemos.wallet.application.transaction.model.TransactionView

data class TelegramAccountTransactionSnapshot(
    val accountId: AccountId,
    val page: PageResult<TransactionView>,
)
