package com.mnemos.profile.application.profile.port.out

import com.mnemos.profile.application.common.pagination.PageResult
import com.mnemos.profile.application.profile.usecase.search.SearchPublicProfilesQuery
import com.mnemos.profile.domain.profile.model.PublicProfile
import java.time.Instant
import java.util.UUID

interface PublicProfileRepository {
    fun ensureExists(
        accountId: UUID,
        cristalixUuid: UUID,
        cristalixNickname: String,
        createdAt: Instant,
    )

    fun save(profile: PublicProfile): PublicProfile

    fun findByAccountId(accountId: UUID): PublicProfile?

    fun findByAccountIdForUpdate(accountId: UUID): PublicProfile?

    fun search(query: SearchPublicProfilesQuery): PageResult<PublicProfile>
}
