package com.mnemos.common.core.model.id

import java.util.UUID

@JvmInline
value class AccountId(val id: UUID) {

    companion object {
        fun generate(): AccountId = AccountId(UUID.randomUUID())
    }

    override fun toString(): String = id.toString()
}