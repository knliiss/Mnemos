package com.mnemos.personalization.application.asset.usecase

import com.mnemos.personalization.application.asset.model.PersonalizationAssetImage
import com.mnemos.personalization.application.asset.port.out.PersonalizationAssetStorage
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.asset.model.PersonalizationAsset
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId
import com.mnemos.personalization.domain.asset.repository.PersonalizationAssetRepository
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service

@Service
class GetPersonalizationAssetService(
    private val assetRepository: PersonalizationAssetRepository,
    private val personalizationRepository: PersonalizationRepository,
    private val storage: PersonalizationAssetStorage,
    private val actorProvider: CurrentPersonalizationActorProvider,
) {
    fun load(id: PersonalizationAssetId): PersonalizationAssetImage? {
        val actor = actorProvider.requireActor()
        val asset = assetRepository.findById(id) ?: return null

        if (!canAccess(actor, asset)) {
            return null
        }

        return storage.load(asset.objectKey)
    }

    private fun canAccess(
        actor: PersonalizationActor,
        asset: PersonalizationAsset,
    ): Boolean {
        if (asset.ownerUserId == actor.userId) {
            return true
        }

        if (personalizationRepository.isAssetReferencedByPublicPersonalization(asset.id)) {
            return true
        }

        return actor.canModeratePersonalization() &&
            personalizationRepository.isAssetReferencedByReviewablePersonalization(asset.id)
    }
}
