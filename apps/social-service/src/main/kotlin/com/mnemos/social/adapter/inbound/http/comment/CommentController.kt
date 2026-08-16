package com.mnemos.social.adapter.inbound.http.comment

import com.mnemos.social.adapter.inbound.http.comment.CommentApiMapper.toResponse
import com.mnemos.social.application.comment.usecase.create.CreateCommentUseCase
import com.mnemos.social.application.comment.usecase.delete.DeleteCommentUseCase
import com.mnemos.social.application.comment.usecase.edit.EditCommentUseCase
import com.mnemos.social.application.comment.usecase.get.GetCommentUseCase
import com.mnemos.social.application.comment.usecase.list.ListCommentsUseCase
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class CommentController(
    private val createCommentUseCase: CreateCommentUseCase,
    private val editCommentUseCase: EditCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val getCommentUseCase: GetCommentUseCase,
    private val listCommentsUseCase: ListCommentsUseCase,
) {
    @PostMapping("/accounts/{accountId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable accountId: UUID,
        @Valid @RequestBody request: CreateCommentRequest,
    ): CommentResponse {
        val comment =
            createCommentUseCase.execute(
                targetAccountId = accountId,
                parentCommentId = request.parentCommentId,
                content = request.content,
            )

        return getCommentUseCase
            .execute(comment.id.id)
            .toResponse()
    }

    @PatchMapping("/comments/{commentId}")
    fun edit(
        @PathVariable commentId: UUID,
        @Valid @RequestBody request: EditCommentRequest,
    ): CommentResponse {
        val comment = editCommentUseCase.execute(commentId, request.content)

        return getCommentUseCase
            .execute(comment.id.id)
            .toResponse()
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable commentId: UUID,
    ) {
        deleteCommentUseCase.execute(commentId)
    }

    @GetMapping("/accounts/{accountId}/comments")
    fun list(
        @PathVariable accountId: UUID,
        pageable: Pageable,
    ): Page<CommentResponse> =
        listCommentsUseCase
            .listRoot(accountId, pageable)
            .map { view -> view.toResponse() }

    @GetMapping("/comments/{commentId}/replies")
    fun replies(
        @PathVariable commentId: UUID,
        pageable: Pageable,
    ): Page<CommentResponse> =
        listCommentsUseCase
            .listReplies(commentId, pageable)
            .map { view -> view.toResponse() }
}
