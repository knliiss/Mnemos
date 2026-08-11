package com.mnemos.common.core.model.id

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class AccountId(val id: UUID) {

    companion object {
        fun generate(): AccountId = AccountId(UuidCreator.getTimeOrderedEpoch())
    }

    override fun toString(): String = id.toString()
}