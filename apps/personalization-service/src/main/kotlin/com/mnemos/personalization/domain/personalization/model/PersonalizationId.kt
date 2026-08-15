package com.mnemos.personalization.domain.personalization.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class PersonalizationId(val id: UUID) {
    companion object {
        fun generate(): PersonalizationId = PersonalizationId(UuidCreator.getTimeOrderedEpoch())
    }

    override fun toString(): String = id.toString()
}
