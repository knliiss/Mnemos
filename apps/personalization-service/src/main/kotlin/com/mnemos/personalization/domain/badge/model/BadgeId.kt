package com.mnemos.personalization.domain.badge.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class BadgeId(val id: UUID) {
    companion object {
        fun generate(): BadgeId = BadgeId(UuidCreator.getTimeOrderedEpoch())
    }

    override fun toString(): String = id.toString()
}
