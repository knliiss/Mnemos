package com.mnemos.social.adapter.inbound.http.commentreaction

import com.mnemos.social.application.reaction.CommentLikeService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/comments/{commentId}/like")
class CommentLikeController(
    private val service: CommentLikeService,
) {
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun like(@PathVariable commentId: UUID) {
        service.like(commentId)
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unlike(@PathVariable commentId: UUID) {
        service.unlike(commentId)
    }

    @GetMapping
    fun state(@PathVariable commentId: UUID): CommentLikeStateResponse =
        CommentLikeStateResponse(
            liked = service.isLiked(commentId),
        )

    @GetMapping("/count")
    fun count(@PathVariable commentId: UUID): CommentLikeCountResponse =
        CommentLikeCountResponse(
            likes = service.count(commentId),
        )
}
