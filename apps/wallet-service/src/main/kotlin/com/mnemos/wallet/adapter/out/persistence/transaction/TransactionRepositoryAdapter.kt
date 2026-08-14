package com.mnemos.wallet.adapter.out.persistence.transaction

import com.mnemos.wallet.adapter.out.persistence.transaction.TransactionPersistenceMapper.toEntity
import com.mnemos.wallet.domain.transaction.model.Transaction
import com.mnemos.wallet.domain.transaction.repository.TransactionRepository
import org.springframework.stereotype.Repository

@Repository
class TransactionRepositoryAdapter(
    private val transactionJpaRepository: TransactionJpaRepository,
) : TransactionRepository {
    override fun create(transaction: Transaction) {
        transactionJpaRepository.save(transaction.toEntity())
    }
}
