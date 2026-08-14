package com.mnemos.wallet.adapter.out.persistence.transaction

import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TransactionJpaRepository : JpaRepository<TransactionEntity, UUID> {
    fun findByOwnerTypeAndOwnerId(
        ownerType: WalletOwnerType,
        ownerId: UUID,
        pageable: Pageable,
    ): Page<TransactionEntity>
}
