package com.mnemos.personalization.application.ownership.event

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceContractMapper.toAvatarFrameSnapshot
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceContractMapper.toNickStyleSnapshot
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceContractMapper.toProfileEffectSnapshot
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceContractMapper.toProfileThemeSnapshot
import com.mnemos.personalization.contracts.event.ProfileAppearanceChangedV1
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Component

@Component
class ProfileAppearanceSnapshotResolver(
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val personalizationRepository: PersonalizationRepository,
) {
    fun resolve(accountId: AccountId): ProfileAppearanceChangedV1.Appearance {
        val owner = PersonalizationOwner.Account(accountId)

        return ProfileAppearanceChangedV1.Appearance(
            nickStyle = activePersonalization(owner, PersonalizationType.NICK_STYLE)?.toNickStyleSnapshot(),
            profileTheme = activePersonalization(owner, PersonalizationType.PROFILE_THEME)?.toProfileThemeSnapshot(),
            avatarFrame = activePersonalization(owner, PersonalizationType.AVATAR_FRAME)?.toAvatarFrameSnapshot(),
            profileEffect = activePersonalization(owner, PersonalizationType.PROFILE_EFFECT)?.toProfileEffectSnapshot(),
            profileBackground =
                activePersonalization(owner, PersonalizationType.PROFILE_BACKGROUND)?.toProfileEffectSnapshot(),
        )
    }

    private fun activePersonalization(
        owner: PersonalizationOwner.Account,
        type: PersonalizationType,
    ): Personalization? {
        val ownership = ownershipRepository.findActive(owner, type) ?: return null

        return personalizationRepository.findById(ownership.personalizationId)
            ?: error("Active personalization ${ownership.personalizationId.id} was not found")
    }
}
