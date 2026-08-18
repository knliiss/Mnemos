package com.mnemos.wallet.adapter.inbound.http

import com.mnemos.wallet.adapter.inbound.http.balance.BalanceResponse
import com.mnemos.wallet.adapter.inbound.http.common.PageRequest
import com.mnemos.wallet.adapter.inbound.http.common.PageResponse
import com.mnemos.wallet.adapter.inbound.http.transaction.TransactionResponse
import com.mnemos.wallet.application.balance.model.BalanceView
import com.mnemos.wallet.application.common.pagination.PageQuery
import com.mnemos.wallet.application.common.pagination.PageResult
import com.mnemos.wallet.application.transaction.model.TransactionView
import com.mnemos.wallet.application.transaction.usecase.list.TransactionSort

object WalletApiMapper {
    fun PageRequest.toQuery(): PageQuery =
        PageQuery(
            page = page,
            size = size,
        )

    fun PageRequest.toSort(): TransactionSort =
        TransactionSort(
            field = sortBy,
            direction = sortDirection,
        )

    fun BalanceView.toResponse(): BalanceResponse =
        BalanceResponse(
            currency = currency,
            amount = amount,
        )

    fun TransactionView.toResponse(): TransactionResponse =
        TransactionResponse(
            id = id,
            requestId = requestId,
            currency = currency,
            type = type,
            source = source,
            sourceReferenceId = sourceReferenceId,
            amount = amount,
            createdAt = createdAt,
        )

    fun PageResult<TransactionView>.toResponse(): PageResponse<TransactionResponse> =
        PageResponse(
            items = items.map { it.toResponse() },
            page = page,
            size = size,
            totalItems = totalItems,
            totalPages = totalPages,
            hasNextPage = hasNextPage,
        )
}
