package com.mnemos.social.application.accountreaction.usecase.set

import com.mnemos.social.application.actor.model.SocialActor

fun interface SetAccountReactionForActorUseCase {
    fun executeForActor(command: SetAccountReactionCommand, actor: SocialActor)
}
