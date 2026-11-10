package com.mnemos.social.application.comment.usecase.edit

import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.domain.comment.model.Comment

fun interface EditCommentForActorUseCase {
    fun executeForActor(command: EditCommentCommand, actor: SocialActor): Comment
}
