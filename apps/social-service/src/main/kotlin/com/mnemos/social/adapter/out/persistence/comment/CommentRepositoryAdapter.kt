package com.mnemos.social.adapter.out.persistence.comment

import com.mnemos.social.adapter.out.persistence.comment.CommentPersistenceMapper.toDomain
import com.mnemos.social.adapter.out.persistence.comment.CommentPersistenceMapper.toEntity
import com.mnemos.social.domain.comment.model.Comment
import com.mnemos.social.domain.comment.model.CommentId
import com.mnemos.social.domain.comment.repository.CommentRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CommentRepositoryAdapter(
    private val repository: CommentJpaRepository,
) : CommentRepository {
    override fun save(comment: Comment): Comment = repository.save(comment.toEntity()).toDomain()

    override fun findById(id: CommentId): Comment? = repository.findById(id.id).orElse(null)?.toDomain()

    override fun findByIdForUpdate(id: CommentId): Comment? = repository.findForUpdate(id.id)?.toDomain()

    override fun findRootByAccountId(
        accountId: UUID,
        pageable: Pageable,
    ): Page<Comment> =
        repository
            .findRootByAccountId(accountId, pageable)
            .map { entity -> entity.toDomain() }

    override fun findReplies(
        parentCommentId: CommentId,
        pageable: Pageable,
    ): Page<Comment> =
        repository
            .findReplies(parentCommentId.id, pageable)
            .map { entity -> entity.toDomain() }
}
