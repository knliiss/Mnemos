package com.mnemos.wallet.application.transaction.usecase.list

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.application.common.pagination.PageQuery

data class ListAccountTransactionsQuery(
    val accountId: AccountId,
    val page: PageQuery,
    val sort: TransactionSort,
)
