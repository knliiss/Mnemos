package com.mnemos.personalization.adapter.out.persistence.personalization

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.adapter.out.persistence.personalization.PersonalizationContentPersistenceMapper.toDocument
import com.mnemos.personalization.adapter.out.persistence.personalization.PersonalizationContentPersistenceMapper.toDomain
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice

object PersonalizationPersistenceMapper {
    fun Personalization.toEntity(): PersonalizationEntity =
        PersonalizationEntity(
            id = id.id,
            creatorUserId = creatorUserId.id,
            type = type,
            origin = origin,
            visibility = visibility,
            name = name,
            content = content.toDocument(),
            priceCurrency = price?.currency,
            priceAmount = price?.amount,
            status = status,
            rejectionReason = rejectionReason,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun PersonalizationEntity.toDomain(): Personalization =
        try {
            Personalization(
                id = PersonalizationId(id),
                creatorUserId = UserId(creatorUserId),
                type = type,
                origin = origin,
                visibility = visibility,
                name = name,
                content = content.toDomain(type),
                price = toPrice(),
                status = status,
                rejectionReason = rejectionReason,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        } catch (exception: IllegalArgumentException) {
            throw IllegalStateException(
                "Persisted personalization $id contains invalid domain state",
                exception,
            )
        }

    private fun PersonalizationEntity.toPrice(): PersonalizationPrice? =
        when {
            priceCurrency == null && priceAmount == null -> null
            priceCurrency != null && priceAmount != null ->
                PersonalizationPrice(
                    currency = requireNotNull(priceCurrency),
                    amount = requireNotNull(priceAmount),
                )

            else -> throw IllegalStateException("Persisted personalization price must contain both currency and amount")
        }
}
