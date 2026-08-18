package com.mnemos.profile.application.profile.port.out

import com.mnemos.profile.domain.profile.model.ProfileAppearance
import java.util.UUID

interface ProfileAppearanceRepository {
    fun save(appearance: ProfileAppearance): ProfileAppearance

    fun findByAccountId(accountId: UUID): ProfileAppearance?

    fun findByAccountIdForUpdate(accountId: UUID): ProfileAppearance?

    fun findByAccountIds(accountIds: Collection<UUID>): Map<UUID, ProfileAppearance>
}
