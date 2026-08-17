package com.mnemos.social.adapter.out.persistence.reaction

import java.io.Serializable
import java.util.UUID

data class CommentLikeEntityId(
    var commentId: UUID? = null,
    var actorAccountId: UUID? = null,
) : Serializable
