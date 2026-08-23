package com.mnemos.personalization.domain.asset.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class PersonalizationAssetId(
    val id: UUID,
) {
    companion object {
        fun generate(): PersonalizationAssetId = PersonalizationAssetId(UuidCreator.getTimeOrderedEpoch())
    }

    override fun toString(): String = id.toString()
}
