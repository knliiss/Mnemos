package com.mnemos.social.domain.comment.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class CommentId(
    val id: UUID,
) {
    companion object {
        fun generate(): CommentId = CommentId(UuidCreator.getTimeOrderedEpoch())
    }
}
