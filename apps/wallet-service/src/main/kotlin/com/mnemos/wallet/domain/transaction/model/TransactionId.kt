package com.mnemos.wallet.domain.transaction.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class TransactionId(
    val id: UUID,
) {
    companion object {
        fun generate(): TransactionId = TransactionId(UuidCreator.getTimeOrderedEpoch())
    }

    override fun toString(): String = id.toString()
}
