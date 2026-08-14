package com.mnemos.wallet.domain.transaction.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class RequestId(
    val id: UUID,
) {
    companion object {
        fun generate(): RequestId = RequestId(UuidCreator.getTimeOrderedEpoch())
    }

    override fun toString(): String = id.toString()
}
