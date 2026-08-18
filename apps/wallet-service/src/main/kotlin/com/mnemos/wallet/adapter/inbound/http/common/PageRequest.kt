package com.mnemos.wallet.adapter.inbound.http.common

import com.mnemos.wallet.application.transaction.usecase.list.TransactionSortDirection
import com.mnemos.wallet.application.transaction.usecase.list.TransactionSortField
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class PageRequest(
    @field:Min(0)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(100)
    val size: Int = 20,
    val sortBy: TransactionSortField = TransactionSortField.CREATED_AT,
    val sortDirection: TransactionSortDirection = TransactionSortDirection.DESC,
)
