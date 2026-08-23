package com.mnemos.personalization.application.asset.usecase

import com.mnemos.personalization.application.asset.port.out.PersonalizationAssetStorage
import com.mnemos.personalization.application.asset.support.PersonalizationAssetImageValidator
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.asset.model.PersonalizationAsset
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId
import com.mnemos.personalization.domain.asset.repository.PersonalizationAssetRepository
import org.springframework.stereotype.Service
import java.time.Clock

@Service
class ManagePersonalizationAssetService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val validator: PersonalizationAssetImageValidator,
    private val repository: PersonalizationAssetRepository,
    private val storage: PersonalizationAssetStorage,
    private val clock: Clock,
) {
    fun upload(
        bytes: ByteArray,
        contentType: String?,
    ): PersonalizationAsset {
        val actor = actorProvider.requireActor()

        if (!actor.canCreateMarketplacePersonalization()) {
            throw PersonalizationAccessDeniedException()
        }

        val image = validator.validate(bytes, contentType)
        val assetId = PersonalizationAssetId.generate()
        val objectKey = "personalizations/${actor.userId.id}/$assetId.png"
        val asset =
            PersonalizationAsset(
                id = assetId,
                ownerUserId = actor.userId,
                objectKey = objectKey,
                contentType = image.contentType,
                sizeBytes = image.bytes.size.toLong(),
                widthPx = image.widthPx,
                heightPx = image.heightPx,
                createdAt = clock.instant(),
            )

        storage.store(objectKey, image)

        return try {
            repository.save(asset)
        } catch (exception: RuntimeException) {
            runCatching { storage.delete(objectKey) }
            throw exception
        }
    }

    fun listMine(): List<PersonalizationAsset> {
        val actor = actorProvider.requireActor()

        if (!actor.canCreateMarketplacePersonalization()) {
            throw PersonalizationAccessDeniedException()
        }

        return repository.findByOwnerUserId(actor.userId)
    }
}
