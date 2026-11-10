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
            val response = client.listComments(accountId, page, PAGE_SIZE)
            ProfileCommentsPageResult.Available(
                ProfileCommentsPage(
                    entries = response.items.map {
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
                    page = response.page,
                    totalPages = response.totalPages,
                    totalItems = response.totalItems,
                ),
            )
        } catch (_: RuntimeException) {
            ProfileCommentsPageResult.Unavailable
        }

    private companion object {
        const val PAGE_SIZE = 3
    }
}
