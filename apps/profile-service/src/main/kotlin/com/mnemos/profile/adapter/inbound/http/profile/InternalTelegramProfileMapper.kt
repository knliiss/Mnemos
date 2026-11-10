package com.mnemos.profile.adapter.inbound.http.profile

import com.mnemos.contracts.profile.internal.v1.InternalTelegramPublicProfileSummary
import com.mnemos.profile.domain.profile.model.ProfilePlayerState
import com.mnemos.profile.domain.profile.model.PublicProfile

object InternalTelegramProfileMapper {
    fun PublicProfile.toInternalSummary(): InternalTelegramPublicProfileSummary {
        val state = playerState
        val hidden = state != null &&
            (state.privacyHidden || state.presenceStatus == ProfilePlayerState.PresenceStatus.HIDDEN)
        return InternalTelegramPublicProfileSummary(
            accountId = accountId,
            cristalixUuid = cristalixUuid,
            nickname = state?.nickname ?: cristalixNickname,
            likes = likes,
            dislikes = dislikes,
            comments = comments,
            presenceStatus = if (hidden) "HIDDEN" else state?.presenceStatus?.name ?: "UNKNOWN",
            realm = if (hidden) null else state?.realm,
            lastSeenOnline = if (hidden) null else state?.lastSeenOnline,
            createdAt = createdAt,
        )
    }
}
