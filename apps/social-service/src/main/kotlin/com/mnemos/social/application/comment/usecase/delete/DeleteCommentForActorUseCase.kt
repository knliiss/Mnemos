package com.mnemos.social.application.comment.usecase.delete

import com.mnemos.social.application.actor.model.SocialActor
import java.util.UUID

fun interface DeleteCommentForActorUseCase {
    fun executeForActor(commentId: UUID, actor: SocialActor)
}
