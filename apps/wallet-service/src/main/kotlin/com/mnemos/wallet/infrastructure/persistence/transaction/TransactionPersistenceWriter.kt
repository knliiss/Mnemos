package com.mnemos.wallet.infrastructure.persistence.transaction

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component

@Component
class TransactionPersistenceWriter(
    private val entityManager: EntityManager,
) {
    fun persist(transaction: TransactionEntity) {
        entityManager.persist(transaction)
    }
}
