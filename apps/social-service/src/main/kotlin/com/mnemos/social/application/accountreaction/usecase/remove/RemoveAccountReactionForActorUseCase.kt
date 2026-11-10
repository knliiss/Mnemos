package com.mnemos.social.application.accountreaction.usecase.remove

import com.mnemos.social.application.actor.model.SocialActor
import java.util.UUID

fun interface RemoveAccountReactionForActorUseCase {
    fun executeForActor(targetAccountId: UUID, actor: SocialActor)
}
