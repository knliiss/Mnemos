package com.mnemos.common.core.model.id

import java.util.UUID

@JvmInline
value class UserId(val id: UUID) {

    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID())
    }

    override fun toString(): String = id.toString()
}
