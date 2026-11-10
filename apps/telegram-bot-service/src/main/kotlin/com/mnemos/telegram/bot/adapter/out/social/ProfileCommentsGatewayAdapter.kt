package com.mnemos.telegram.bot.adapter.out.social

import com.mnemos.telegram.bot.application.profilecomments.ProfileCommentEntry
import com.mnemos.telegram.bot.application.profilecomments.ProfileCommentsPage
import com.mnemos.telegram.bot.application.profilecomments.ProfileCommentsPageResult
import com.mnemos.telegram.bot.application.profilecomments.port.out.ProfileCommentsGateway
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProfileCommentsGatewayAdapter(
    private val client: SocialServiceClient,
) : ProfileCommentsGateway {
    override fun list(accountId: UUID, page: Int): ProfileCommentsPageResult =
        try {
            client.listComments(accountId, page, PAGE_SIZE).toResult()
        } catch (_: RuntimeException) {
            ProfileCommentsPageResult.Unavailable
        }

    override fun replies(accountId: UUID, rootCommentId: UUID, page: Int): ProfileCommentsPageResult =
        try {
            client.listReplies(accountId, rootCommentId, page, PAGE_SIZE).toResult()
        } catch (exception: feign.FeignException) {
            if (exception.status() == 404) ProfileCommentsPageResult.NotFound
            else ProfileCommentsPageResult.Unavailable
        } catch (_: RuntimeException) {
            ProfileCommentsPageResult.Unavailable
        }

    private fun com.mnemos.social.contracts.internal.v1.InternalTelegramCommentPage.toResult(): ProfileCommentsPageResult =
        ProfileCommentsPageResult.Available(
            ProfileCommentsPage(
                entries = items.map {
                    ProfileCommentEntry(
                        id = it.id,
                        authorAccountId = it.authorAccountId,
                        content = if (it.deleted) null else it.content,
                        deleted = it.deleted,
                        likes = it.likes,
                        replies = it.replies,
                        createdAt = it.createdAt,
                    )
                },
                page = page,
                totalPages = totalPages,
                totalItems = totalItems,
            ),
        )

    private companion object {
        const val PAGE_SIZE = 3
    }
}
