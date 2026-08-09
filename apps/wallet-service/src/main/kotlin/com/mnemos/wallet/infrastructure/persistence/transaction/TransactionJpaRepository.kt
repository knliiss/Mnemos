package com.mnemos.wallet.infrastructure.persistence.transaction

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TransactionJpaRepository : JpaRepository<TransactionEntity, UUID> {
    fun findByRequestId(requestId: UUID): TransactionEntity?

    fun findByAccountId(
        accountId: UUID,
        pageable: Pageable,
    ): Page<TransactionEntity>
}
