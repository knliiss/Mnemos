package com.mnemos.telegram.bot.application.widget.profilecomments

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.profile.model.ProfilePresenceStatus
import com.mnemos.telegram.bot.application.profile.model.PublicProfileDetailResult
import com.mnemos.telegram.bot.application.profile.model.PublicProfileSummary
import com.mnemos.telegram.bot.application.profile.port.out.PublicProfileGateway
import com.mnemos.telegram.bot.application.profilecomments.ProfileCommentEntry
import com.mnemos.telegram.bot.application.profilecomments.ProfileCommentsPage
import com.mnemos.telegram.bot.application.profilecomments.ProfileCommentsPageResult
import com.mnemos.telegram.bot.application.profilecomments.port.out.ProfileCommentsGateway
import com.mnemos.telegram.bot.application.widget.profilecomments.port.out.ProfileCommentsWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class OpenProfileCommentsWidgetServiceTest {
    private val profiles = mock<PublicProfileGateway>()
    private val comments = mock<ProfileCommentsGateway>()
    private val publisher = mock<ProfileCommentsWidgetPublisher>()
    private val service = OpenProfileCommentsWidgetService(
        TelegramWidgetProperties(enabled = true), profiles, comments, publisher,
    )
    private val profileId = UUID.randomUUID()
    private val commentId = UUID.randomUUID()
    private val now = Instant.parse("2026-11-10T15:00:00Z")

    @Test
    fun commentsUseValidatedPublicProfileAndLimitUntrustedContent() {
        whenever(profiles.get(profileId)).thenReturn(PublicProfileDetailResult.Available(profile()))
        whenever(comments.list(profileId, 0)).thenReturn(
            ProfileCommentsPageResult.Available(
                ProfileCommentsPage(
                    listOf(entry(content = "Ж".repeat(300))),
                    0, 2, 5,
                ),
            ),
        )

        val result = service.load(11L, profileId, 0) as ProfileCommentsWidgetSnapshotResult.Available

        assertEquals(profileId, result.view.profileId)
        assertEquals(5, result.view.totalItems)
        assertTrue(result.view.comments.single().content.toByteArray(Charsets.UTF_8).size <= 160)
        verify(comments).list(profileId, 0)
    }

    @Test
    fun repliesRemainBoundToTheSelectedProfileAndRoot() {
        whenever(profiles.get(profileId)).thenReturn(PublicProfileDetailResult.Available(profile()))
        whenever(comments.replies(profileId, commentId, 1)).thenReturn(
            ProfileCommentsPageResult.Available(ProfileCommentsPage(listOf(entry()), 1, 2, 4)),
        )

        val result = service.load(11L, profileId, 1, commentId) as ProfileCommentsWidgetSnapshotResult.Available

        assertEquals(commentId, result.view.rootCommentId)
        assertEquals(1, result.view.page)
        verify(comments).replies(profileId, commentId, 1)
    }

    @Test
    fun missingProfileDoesNotAskSocialServiceForComments() {
        whenever(profiles.get(profileId)).thenReturn(PublicProfileDetailResult.NotFound)

        assertEquals(ProfileCommentsWidgetSnapshotResult.NotFound, service.load(11L, profileId, 0))
        verifyNoInteractions(comments)
    }

    @Test
    fun disabledWidgetDoesNotReadServices() {
        val disabled = OpenProfileCommentsWidgetService(
            TelegramWidgetProperties(enabled = false), profiles, comments, publisher,
        )
        assertEquals(
            TelegramDeliveryResult.PermanentFailure("widgets-disabled"),
            disabled.execute(11L, profileId),
        )
        verifyNoInteractions(profiles, comments, publisher)
    }

    private fun profile() = PublicProfileSummary(
        accountId = profileId,
        nickname = "Kirito",
        cristalixUuid = UUID.randomUUID(),
        likes = 4,
        dislikes = 1,
        comments = 5,
        presenceStatus = ProfilePresenceStatus.UNKNOWN,
        realm = null,
        lastSeenOnline = null,
        createdAt = now,
    )

    private fun entry(content: String = "Коментар") = ProfileCommentEntry(
        id = commentId,
        authorAccountId = UUID.randomUUID(),
        content = content,
        deleted = false,
        likes = 2,
        replies = 1,
        createdAt = now,
    )
}
