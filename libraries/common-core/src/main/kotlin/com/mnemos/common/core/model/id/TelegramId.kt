package com.mnemos.common.core.model.id

import java.util.UUID

@JvmInline
value class TelegramId(val value: Long) {

    companion object {
        fun generate(): TelegramId = TelegramId(UUID.randomUUID().mostSignificantBits)
    }

    override fun toString(): String = value.toString()
}