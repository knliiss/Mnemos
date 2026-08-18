package com.mnemos.wallet.adapter.out.persistence.transaction

import com.mnemos.wallet.adapter.out.persistence.transaction.TransactionViewPersistenceMapper.toView
import com.mnemos.wallet.application.common.pagination.PageQuery
import com.mnemos.wallet.application.common.pagination.PageResult
import com.mnemos.wallet.application.transaction.model.TransactionView
import com.mnemos.wallet.application.transaction.port.out.TransactionReader
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
    ): PageResult<TransactionView> {
        val pageable =
            PageRequest.of(
                page.page,
                page.size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")),
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
}
