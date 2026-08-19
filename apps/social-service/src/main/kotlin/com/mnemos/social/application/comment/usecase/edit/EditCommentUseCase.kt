package com.mnemos.social.application.comment.usecase.edit

import com.mnemos.social.domain.comment.model.Comment

interface EditCommentUseCase {
    fun execute(command: EditCommentCommand): Comment
}
