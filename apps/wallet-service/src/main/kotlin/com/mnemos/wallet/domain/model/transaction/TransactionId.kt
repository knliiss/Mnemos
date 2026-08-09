package com.mnemos.wallet.domain.model.transaction

import java.util.UUID

@JvmInline
value class TransactionId(
    val id: UUID,
) {
    companion object {
        fun generate(): TransactionId = TransactionId(UUID.randomUUID())
    }
}
