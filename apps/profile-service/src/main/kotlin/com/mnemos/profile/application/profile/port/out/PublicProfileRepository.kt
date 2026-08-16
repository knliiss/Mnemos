package com.mnemos.profile.application.profile.port.out

import com.mnemos.profile.domain.profile.model.PublicProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    fun search(
        query: String,
        pageable: Pageable,
    ): Page<PublicProfile>
}
