package com.mnemos.wallet.infrastructure.persistence.transaction

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.api.dto.TransactionSummaryResponse
import com.mnemos.wallet.api.dto.toSummaryResponse
import com.mnemos.wallet.domain.model.transaction.RequestId
import com.mnemos.wallet.domain.model.transaction.Transaction
import com.mnemos.wallet.domain.repository.TransactionRepository
import com.mnemos.wallet.infrastructure.persistence.transaction.TransactionPersistenceMapper.toDomain
import com.mnemos.wallet.infrastructure.persistence.transaction.TransactionPersistenceMapper.toEntity
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class TransactionRepositoryAdapter(
    private val transactionJpaRepository: TransactionJpaRepository,
    private val transactionPersistenceWriter: TransactionPersistenceWriter,
) : TransactionRepository {
    override fun findByRequestId(requestId: RequestId): Transaction? = transactionJpaRepository.findByRequestId(requestId.id)?.toDomain()

    override fun create(transaction: Transaction) {
        transactionPersistenceWriter.persist(transaction.toEntity())
    }

    override fun findByAccountId(
        accountId: AccountId,
        page: Pageable,
    ): List<TransactionSummaryResponse> =
        transactionJpaRepository
            .findByAccountId(
                accountId = accountId.id,
                page,
            ).map { it.toSummaryResponse() }
            .toList()
}
