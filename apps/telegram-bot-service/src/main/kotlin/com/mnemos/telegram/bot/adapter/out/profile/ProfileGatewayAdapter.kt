package com.mnemos.telegram.bot.adapter.out.profile

import com.mnemos.contracts.profile.internal.v1.InternalTelegramPublicProfileSummary
import com.mnemos.telegram.bot.application.profile.model.ProfilePresenceStatus
import com.mnemos.telegram.bot.application.profile.model.PublicProfileDetailResult
import com.mnemos.telegram.bot.application.profile.model.PublicProfileSearchPage
import com.mnemos.telegram.bot.application.profile.model.PublicProfileSearchResult
import com.mnemos.telegram.bot.application.profile.model.PublicProfileSummary
import com.mnemos.telegram.bot.application.profile.port.out.PublicProfileGateway
import feign.FeignException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProfileGatewayAdapter(
    private val client: ProfileServiceClient,
) : PublicProfileGateway {
    override fun search(query: String, page: Int, size: Int): PublicProfileSearchResult =
        try {
            val response = client.search(query, page, size)
            PublicProfileSearchResult.Available(
                PublicProfileSearchPage(
                    items = response.items.map { it.toModel() },
                    page = response.page,
                    totalPages = response.totalPages,
                    totalItems = response.totalItems,
                ),
            )
        } catch (_: RuntimeException) {
            PublicProfileSearchResult.Unavailable
        }

    override fun get(accountId: UUID): PublicProfileDetailResult =
        try {
            client.get(accountId)?.let { PublicProfileDetailResult.Available(it.toModel()) }
                ?: PublicProfileDetailResult.NotFound
        } catch (exception: FeignException) {
            if (exception.status() == 404) PublicProfileDetailResult.NotFound
            else PublicProfileDetailResult.Unavailable
        } catch (_: RuntimeException) {
            PublicProfileDetailResult.Unavailable
        }

    private fun InternalTelegramPublicProfileSummary.toModel(): PublicProfileSummary {
        val presence = ProfilePresenceStatus.entries.firstOrNull { it.name == presenceStatus }
            ?: ProfilePresenceStatus.UNKNOWN
        return PublicProfileSummary(
            accountId = accountId,
            nickname = nickname,
            cristalixUuid = cristalixUuid,
            likes = likes,
            dislikes = dislikes,
            comments = comments,
            presenceStatus = presence,
            realm = if (presence == ProfilePresenceStatus.HIDDEN) null else realm,
            lastSeenOnline = if (presence == ProfilePresenceStatus.HIDDEN) null else lastSeenOnline,
            createdAt = createdAt,
        )
    }
}
