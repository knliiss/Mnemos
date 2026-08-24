package com.mnemos.personalization.domain.personalization.repository

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus

interface PersonalizationRepository {
    fun save(personalization: Personalization): Personalization

    fun findById(id: PersonalizationId): Personalization?

    fun findByIdForUpdate(id: PersonalizationId): Personalization? = findById(id)

    fun findByCreatorUserId(creatorUserId: UserId): List<Personalization>

    fun findByStatus(status: PersonalizationStatus): List<Personalization>

    fun isAssetReferencedByPublicPersonalization(assetId: PersonalizationAssetId): Boolean

    fun isAssetReferencedByReviewablePersonalization(assetId: PersonalizationAssetId): Boolean
}
