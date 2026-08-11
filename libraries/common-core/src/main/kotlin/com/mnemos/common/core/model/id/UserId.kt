package com.mnemos.common.core.model.id

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class UserId(val id: UUID) {

    companion object {
        fun generate(): UserId = UserId(UuidCreator.getTimeOrderedEpoch())
    }

    override fun toString(): String = id.toString()
}
