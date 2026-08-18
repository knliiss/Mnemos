package com.mnemos.wallet.application.transaction.usecase.list

import com.mnemos.wallet.application.common.pagination.PageQuery

data class ListCurrentUserTransactionsQuery(
    val page: PageQuery,
    val sort: TransactionSort,
)
