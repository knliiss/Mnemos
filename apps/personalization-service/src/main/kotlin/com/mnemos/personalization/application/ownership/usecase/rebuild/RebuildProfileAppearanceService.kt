package com.mnemos.personalization.application.ownership.usecase.rebuild

import com.mnemos.personalization.application.ownership.event.ProfileAppearanceEventPublisher
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RebuildProfileAppearanceService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val profileAppearanceEventPublisher: ProfileAppearanceEventPublisher,
) {
    @Transactional
    fun execute(): Int {
        val actor = actorProvider.requireActor()
        val accountIds = ownershipRepository.findActiveAccountIds(APPEARANCE_TYPES)

        accountIds.forEach { accountId ->
            profileAppearanceEventPublisher.publish(
                accountId = accountId,
                actorUserId = actor.userId,
            )
        }

        return accountIds.size
    }

    private companion object {
        val APPEARANCE_TYPES =
            setOf(
                PersonalizationType.NICK_STYLE,
                PersonalizationType.PROFILE_THEME,
                PersonalizationType.AVATAR_FRAME,
                PersonalizationType.PROFILE_EFFECT,
            )
    }
}
