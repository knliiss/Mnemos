package com.mnemos.personalization.application.asset.support

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.domain.asset.model.PersonalizationAsset
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId
import com.mnemos.personalization.domain.asset.repository.PersonalizationAssetRepository
import com.mnemos.personalization.domain.personalization.model.HexColor
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.VisualLayer
import com.mnemos.personalization.domain.personalization.model.VisualLayerAnimation
import com.mnemos.personalization.domain.personalization.model.VisualLayerBlendMode
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PersonalizationContentAssetValidatorTest {
    @Test
    fun `accepts visual layer backed by an asset owned by creator`() {
        val ownerUserId = UserId.generate()
        val assetId = PersonalizationAssetId.generate()
        val repository = FakePersonalizationAssetRepository(setOf(assetId to ownerUserId))
        val validator = PersonalizationContentAssetValidator(repository)

        assertDoesNotThrow {
            validator.requireOwnedAssets(
                ownerUserId = ownerUserId,
                content = profileTheme(assetId),
            )
        }
    }

    @Test
    fun `rejects visual layer backed by another users asset`() {
        val ownerUserId = UserId.generate()
        val anotherUserId = UserId.generate()
        val assetId = PersonalizationAssetId.generate()
        val repository = FakePersonalizationAssetRepository(setOf(assetId to anotherUserId))
        val validator = PersonalizationContentAssetValidator(repository)

        assertThrows(PersonalizationAccessDeniedException::class.java) {
            validator.requireOwnedAssets(
                ownerUserId = ownerUserId,
                content = profileTheme(assetId),
            )
        }
    }

    private fun profileTheme(assetId: PersonalizationAssetId): PersonalizationContent.ProfileTheme =
        PersonalizationContent.ProfileTheme(
            background = HexColor("#080B08"),
            surface = HexColor("#151812"),
            accent = HexColor("#CBFF2D"),
            text = HexColor("#F3F5EC"),
            border = HexColor("#343B2E"),
            glow = null,
            layers =
                listOf(
                    VisualLayer(
                        id = "layer-1",
                        assetId = assetId,
                        xPercent = 50,
                        yPercent = 50,
                        widthPercent = 60,
                        opacity = 100,
                        rotationDegrees = 0,
                        blendMode = VisualLayerBlendMode.NORMAL,
                        animation = VisualLayerAnimation.NONE,
                        zIndex = 1,
                    ),
                ),
        )

    private class FakePersonalizationAssetRepository(
        private val ownerships: Set<Pair<PersonalizationAssetId, UserId>>,
    ) : PersonalizationAssetRepository {
        override fun save(asset: PersonalizationAsset): PersonalizationAsset = asset

        override fun findById(id: PersonalizationAssetId): PersonalizationAsset? = null

        override fun findByOwnerUserId(ownerUserId: UserId): List<PersonalizationAsset> = emptyList()

        override fun existsByIdAndOwnerUserId(
            id: PersonalizationAssetId,
            ownerUserId: UserId,
        ): Boolean = id to ownerUserId in ownerships
    }
}
