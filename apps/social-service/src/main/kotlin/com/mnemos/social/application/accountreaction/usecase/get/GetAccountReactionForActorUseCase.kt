package com.mnemos.social.application.accountreaction.usecase.get

import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.domain.reaction.model.AccountReactionType
import java.util.UUID

fun interface GetAccountReactionForActorUseCase {
    fun executeForActor(targetAccountId: UUID, actor: SocialActor): AccountReactionType?
}
