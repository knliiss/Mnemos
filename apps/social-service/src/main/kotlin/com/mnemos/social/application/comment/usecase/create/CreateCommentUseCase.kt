package com.mnemos.social.application.comment.usecase.create

import com.mnemos.social.domain.comment.model.Comment

interface CreateCommentUseCase {
    fun execute(command: CreateCommentCommand): Comment
}
