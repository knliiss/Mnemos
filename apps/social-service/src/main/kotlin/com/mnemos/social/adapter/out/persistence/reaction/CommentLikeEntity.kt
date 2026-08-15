package com.mnemos.social.adapter.out.persistence.reaction

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.time.Instant
import java.util.UUID

data class CommentLikeEntityId(
    var commentId: UUID? = null,
    var actorAccountId: UUID? = null,
) : Serializable

@Entity
@Table(name = "comment_likes")
@IdClass(CommentLikeEntityId::class)
class CommentLikeEntity(
    @Id
    @Column(name = "comment_id", nullable = false)
    var commentId: UUID,

    @Id
    @Column(name = "actor_account_id", nullable = false)
    var actorAccountId: UUID,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
)
