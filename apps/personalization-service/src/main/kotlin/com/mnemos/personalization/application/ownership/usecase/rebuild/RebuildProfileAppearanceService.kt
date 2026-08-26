package com.mnemos.personalization.application.ownership.usecase.rebuild

import com.mnemos.common.core.model.id.UserId
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

        return rebuild(actor.userId)
    }

    @Transactional
    fun executeSystem(): Int = rebuild(actorUserId = null)

    private fun rebuild(actorUserId: UserId?): Int {
        val accountIds = ownershipRepository.findActiveAccountIds(APPEARANCE_TYPES)

        accountIds.forEach { accountId ->
            profileAppearanceEventPublisher.publish(
                accountId = accountId,
                actorUserId = actorUserId,
            )
        }

        return accountIds.size
    }

    private companion object {
        val APPEARANCE_TYPES =
            setOf(
                PersonalizationType.NICK_STYLE,
                PersonalizationType.PROFILE_THEME,
                PersonalizationType.PROFILE_BACKGROUND,
                PersonalizationType.AVATAR_FRAME,
                PersonalizationType.PROFILE_EFFECT,
            )
    }
}
