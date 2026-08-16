package com.mnemos.social.adapter.out.persistence.comment

import com.mnemos.social.adapter.out.persistence.comment.CommentPersistenceMapper.toDomain
import com.mnemos.social.adapter.out.persistence.comment.CommentPersistenceMapper.toEntity
import com.mnemos.social.domain.comment.model.Comment
import com.mnemos.social.domain.comment.model.CommentId
import com.mnemos.social.domain.comment.repository.CommentRepository
import org.springframework.stereotype.Component

@Component
class CommentRepositoryAdapter(
    private val repository: CommentJpaRepository,
) : CommentRepository {
    override fun save(comment: Comment): Comment = repository.save(comment.toEntity()).toDomain()

    override fun findById(id: CommentId): Comment? = repository.findById(id.id).orElse(null)?.toDomain()

    override fun findByIdForUpdate(id: CommentId): Comment? = repository.findForUpdate(id.id)?.toDomain()
}
