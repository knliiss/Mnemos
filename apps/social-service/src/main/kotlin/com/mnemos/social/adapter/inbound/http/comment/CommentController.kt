package com.mnemos.social.adapter.inbound.http.comment

import com.mnemos.social.adapter.inbound.http.comment.CommentApiMapper.toResponse
import com.mnemos.social.application.comment.CommentService
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
    private val service: CommentService,
) {
    @PostMapping("/accounts/{accountId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable accountId: UUID,
        @Valid @RequestBody request: CreateCommentRequest,
    ): CommentResponse = service.create(accountId, request.parentCommentId, request.content).toResponse()

    @PatchMapping("/comments/{commentId}")
    fun edit(
        @PathVariable commentId: UUID,
        @Valid @RequestBody request: EditCommentRequest,
    ): CommentResponse = service.edit(commentId, request.content).toResponse()

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable commentId: UUID) {
        service.delete(commentId)
    }

    @GetMapping("/accounts/{accountId}/comments")
    fun list(
        @PathVariable accountId: UUID,
        pageable: Pageable,
    ): Page<CommentResponse> = service.listRoot(accountId, pageable).map { it.toResponse() }

    @GetMapping("/comments/{commentId}/replies")
    fun replies(
        @PathVariable commentId: UUID,
        pageable: Pageable,
    ): Page<CommentResponse> = service.listReplies(commentId, pageable).map { it.toResponse() }
}
