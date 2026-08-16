package com.mnemos.profile.application.profile.port.out

import com.mnemos.profile.domain.profile.model.PublicProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface PublicProfileRepository {
    fun save(profile: PublicProfile): PublicProfile

    fun findByAccountId(accountId: UUID): PublicProfile?

    fun findByAccountIdForUpdate(accountId: UUID): PublicProfile?

    fun search(
        query: String,
        pageable: Pageable,
    ): Page<PublicProfile>
}
