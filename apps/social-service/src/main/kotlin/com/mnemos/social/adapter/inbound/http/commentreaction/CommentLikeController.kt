package com.mnemos.social.adapter.inbound.http.commentreaction

import com.mnemos.social.application.commentlike.usecase.count.GetCommentLikeCountUseCase
import com.mnemos.social.application.commentlike.usecase.like.LikeCommentUseCase
import com.mnemos.social.application.commentlike.usecase.state.GetCommentLikeStateUseCase
import com.mnemos.social.application.commentlike.usecase.unlike.UnlikeCommentUseCase
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
    private val likeCommentUseCase: LikeCommentUseCase,
    private val unlikeCommentUseCase: UnlikeCommentUseCase,
    private val getCommentLikeStateUseCase: GetCommentLikeStateUseCase,
    private val getCommentLikeCountUseCase: GetCommentLikeCountUseCase,
) {
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun like(
        @PathVariable commentId: UUID,
    ) {
        likeCommentUseCase.execute(commentId)
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unlike(
        @PathVariable commentId: UUID,
    ) {
        unlikeCommentUseCase.execute(commentId)
    }

    @GetMapping
    fun state(
        @PathVariable commentId: UUID,
    ): CommentLikeStateResponse =
        CommentLikeStateResponse(
            liked = getCommentLikeStateUseCase.execute(commentId),
        )

    @GetMapping("/count")
    fun count(
        @PathVariable commentId: UUID,
    ): CommentLikeCountResponse =
        CommentLikeCountResponse(
            likes = getCommentLikeCountUseCase.execute(commentId),
        )
}
