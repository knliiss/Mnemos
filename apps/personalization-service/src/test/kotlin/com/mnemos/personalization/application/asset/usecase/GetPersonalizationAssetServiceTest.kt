package com.mnemos.personalization.application.asset.usecase

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.personalization.application.asset.model.PersonalizationAssetImage
import com.mnemos.personalization.application.asset.port.out.PersonalizationAssetStorage
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.asset.model.PersonalizationAsset
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId
import com.mnemos.personalization.domain.asset.repository.PersonalizationAssetRepository
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class GetPersonalizationAssetServiceTest {
    private val assetRepository = mock<PersonalizationAssetRepository>()
    private val personalizationRepository = mock<PersonalizationRepository>()
    private val storage = mock<PersonalizationAssetStorage>()
    private val actorProvider = mock<CurrentPersonalizationActorProvider>()

    private val service =
        GetPersonalizationAssetService(
            assetRepository = assetRepository,
            personalizationRepository = personalizationRepository,
            storage = storage,
            actorProvider = actorProvider,
        )

    @Test
    fun `asset owner can load private asset`() {
        val ownerUserId = UserId.generate()
        val asset = asset(ownerUserId)
        val image = image()

        whenever(actorProvider.requireActor())
            .thenReturn(actor(ownerUserId))
        whenever(assetRepository.findById(asset.id))
            .thenReturn(asset)
        whenever(storage.load(asset.objectKey))
            .thenReturn(image)

        val result = service.load(asset.id)

        assertSame(image, result)
        verify(personalizationRepository, never()).isAssetReferencedByPublicPersonalization(any())
        verify(personalizationRepository, never()).isAssetReferencedByReviewablePersonalization(any())
    }

    @Test
    fun `ordinary user cannot load private asset owned by another user`() {
        val asset = asset(UserId.generate())

        whenever(actorProvider.requireActor())
            .thenReturn(actor(UserId.generate()))
        whenever(assetRepository.findById(asset.id))
            .thenReturn(asset)
        whenever(personalizationRepository.isAssetReferencedByPublicPersonalization(asset.id))
            .thenReturn(false)

        val result = service.load(asset.id)

        assertNull(result)
        verify(storage, never()).load(any())
        verify(personalizationRepository, never()).isAssetReferencedByReviewablePersonalization(any())
    }

    @Test
    fun `ordinary user can load asset referenced by public personalization`() {
        val asset = asset(UserId.generate())
        val image = image()

        whenever(actorProvider.requireActor())
            .thenReturn(actor(UserId.generate()))
        whenever(assetRepository.findById(asset.id))
            .thenReturn(asset)
        whenever(personalizationRepository.isAssetReferencedByPublicPersonalization(asset.id))
            .thenReturn(true)
        whenever(storage.load(asset.objectKey))
            .thenReturn(image)

        val result = service.load(asset.id)

        assertSame(image, result)
        verify(personalizationRepository, never()).isAssetReferencedByReviewablePersonalization(any())
    }

    @Test
    fun `personalization moderator can load asset referenced by reviewable personalization`() {
        val asset = asset(UserId.generate())
        val image = image()
        val moderator =
            PersonalizationActor(
                userId = UserId.generate(),
                roles = setOf(UserRole.PERSONALIZATION_MODERATOR),
            )

        whenever(actorProvider.requireActor())
            .thenReturn(moderator)
        whenever(assetRepository.findById(asset.id))
            .thenReturn(asset)
        whenever(personalizationRepository.isAssetReferencedByPublicPersonalization(asset.id))
            .thenReturn(false)
        whenever(personalizationRepository.isAssetReferencedByReviewablePersonalization(asset.id))
            .thenReturn(true)
        whenever(storage.load(asset.objectKey))
            .thenReturn(image)

        val result = service.load(asset.id)

        assertSame(image, result)
    }

    @Test
    fun `personalization moderator cannot load unreferenced private asset`() {
        val asset = asset(UserId.generate())
        val moderator =
            PersonalizationActor(
                userId = UserId.generate(),
                roles = setOf(UserRole.PERSONALIZATION_MODERATOR),
            )

        whenever(actorProvider.requireActor())
            .thenReturn(moderator)
        whenever(assetRepository.findById(asset.id))
            .thenReturn(asset)
        whenever(personalizationRepository.isAssetReferencedByPublicPersonalization(asset.id))
            .thenReturn(false)
        whenever(personalizationRepository.isAssetReferencedByReviewablePersonalization(asset.id))
            .thenReturn(false)

        val result = service.load(asset.id)

        assertNull(result)
        verify(storage, never()).load(any())
    }

    private fun actor(userId: UserId): PersonalizationActor =
        PersonalizationActor(
            userId = userId,
            roles = setOf(UserRole.USER),
        )

    private fun asset(ownerUserId: UserId): PersonalizationAsset {
        val id = PersonalizationAssetId.generate()

        return PersonalizationAsset(
            id = id,
            ownerUserId = ownerUserId,
            objectKey = "personalizations/${ownerUserId.id}/$id.png",
            contentType = "image/png",
            sizeBytes = 4,
            widthPx = 64,
            heightPx = 64,
            createdAt = Instant.parse("2026-08-24T00:00:00Z"),
        )
    }

    private fun image(): PersonalizationAssetImage =
        PersonalizationAssetImage(
            bytes = byteArrayOf(1, 2, 3, 4),
            contentType = "image/png",
            widthPx = 64,
            heightPx = 64,
        )
}
