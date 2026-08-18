package com.mnemos.wallet.adapter.out.persistence.transaction

import com.mnemos.wallet.application.common.pagination.PageQuery
import com.mnemos.wallet.application.common.pagination.PageResult
import com.mnemos.wallet.application.transaction.model.TransactionView
import com.mnemos.wallet.application.transaction.port.out.TransactionReader
import com.mnemos.wallet.application.transaction.usecase.list.TransactionSort
import com.mnemos.wallet.application.transaction.usecase.list.TransactionSortDirection
import com.mnemos.wallet.application.transaction.usecase.list.TransactionSortField
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

@Repository
class TransactionReaderAdapter(
    private val transactionJpaRepository: TransactionJpaRepository,
) : TransactionReader {
    override fun findByOwner(
        owner: WalletOwner,
        page: PageQuery,
        sort: TransactionSort,
    ): PageResult<TransactionView> {
        val pageable =
            PageRequest.of(
                page.page,
                page.size,
                sort.toSpringSort(),
            )

        val result =
            transactionJpaRepository.findByOwnerTypeAndOwnerId(
                ownerType = owner.type,
                ownerId = owner.id,
                pageable = pageable,
            )

        return PageResult(
            items = result.content.map { it.toView() },
            page = result.number,
            size = result.size,
            totalItems = result.totalElements,
            totalPages = result.totalPages,
            hasNextPage = result.hasNext(),
        )
    }

    private fun TransactionSort.toSpringSort(): Sort {
        val springDirection =
            when (this.direction) {
                TransactionSortDirection.ASC -> Sort.Direction.ASC
                TransactionSortDirection.DESC -> Sort.Direction.DESC
            }

        val primary =
            when (field) {
                TransactionSortField.CREATED_AT -> Sort.Order(springDirection, "createdAt")
                TransactionSortField.AMOUNT -> Sort.Order(springDirection, "amount")
            }

        return when (field) {
            TransactionSortField.CREATED_AT ->
                Sort.by(
                    primary,
                    Sort.Order(springDirection, "id"),
                )

            TransactionSortField.AMOUNT ->
                Sort.by(
                    primary,
                    Sort.Order.desc("createdAt"),
                    Sort.Order.desc("id"),
                )
        }
    }

    private fun TransactionEntity.toView(): TransactionView =
        TransactionView(
            id = id,
            requestId = requestId,
            currency = currency,
            type = type,
            source = source,
            sourceReferenceId = sourceReferenceId,
            amount = amount,
            createdAt = createdAt,
        )
}
