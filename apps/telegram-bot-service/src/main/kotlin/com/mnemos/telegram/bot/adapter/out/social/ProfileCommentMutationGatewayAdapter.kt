package com.mnemos.telegram.bot.adapter.out.social

import com.mnemos.social.contracts.internal.v1.InternalTelegramCreateCommentRequest
import com.mnemos.telegram.bot.application.profilecomments.ProfileCommentCreationResult
import com.mnemos.telegram.bot.application.profilecomments.port.out.ProfileCommentMutationGateway
import feign.FeignException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProfileCommentMutationGatewayAdapter(
    private val client: SocialServiceClient,
) : ProfileCommentMutationGateway {
    override fun create(
        telegramId: Long,
        targetAccountId: UUID,
        expectedActorAccountId: UUID,
        operationId: UUID,
        parentCommentId: UUID?,
        content: String,
    ): ProfileCommentCreationResult =
        try {
            val response = client.createComment(
                telegramId,
                targetAccountId,
                InternalTelegramCreateCommentRequest(
                    operationId = operationId,
                    expectedActorAccountId = expectedActorAccountId,
                    parentCommentId = parentCommentId,
                    content = content,
                ),
            )
            ProfileCommentCreationResult.Created(response.commentId)
        } catch (exception: FeignException) {
            when (exception.status()) {
                401, 403, 404 -> ProfileCommentCreationResult.AccountRequired
                409 -> ProfileCommentCreationResult.Conflict
                400, 422 -> ProfileCommentCreationResult.Invalid
                else -> ProfileCommentCreationResult.Unavailable
            }
        } catch (_: RuntimeException) {
            ProfileCommentCreationResult.Unavailable
        }
}
