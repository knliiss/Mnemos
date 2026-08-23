package com.mnemos.personalization.application.asset.support

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.domain.asset.repository.PersonalizationAssetRepository
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.VisualLayer
import org.springframework.stereotype.Component

@Component
class PersonalizationContentAssetValidator(
    private val repository: PersonalizationAssetRepository,
) {
    fun requireOwnedAssets(
        ownerUserId: UserId,
        content: PersonalizationContent,
    ) {
        content.visualLayers().forEach { layer ->
            if (!repository.existsByIdAndOwnerUserId(layer.assetId, ownerUserId)) {
                throw PersonalizationAccessDeniedException()
            }
        }
    }

    private fun PersonalizationContent.visualLayers(): List<VisualLayer> =
        when (this) {
            is PersonalizationContent.ProfileTheme -> layers
            is PersonalizationContent.AvatarFrame -> layers
            is PersonalizationContent.ProfileEffect -> layers
            is PersonalizationContent.NickStyle,
            is PersonalizationContent.NotificationPack,
            -> emptyList()
        }
}
