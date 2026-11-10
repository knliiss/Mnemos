package com.mnemos.social.application.comment.usecase.create

import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.domain.comment.model.Comment

fun interface CreateCommentForActorUseCase {
    fun executeForActor(command: CreateCommentCommand, actor: SocialActor): Comment
}
