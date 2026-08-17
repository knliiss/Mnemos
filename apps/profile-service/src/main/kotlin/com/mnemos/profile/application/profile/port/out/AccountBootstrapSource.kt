package com.mnemos.profile.application.profile.port.out

import com.mnemos.profile.application.profile.model.AccountBootstrapSnapshot
import java.util.UUID

interface AccountBootstrapSource {
    fun fetch(
        afterAccountId: UUID?,
        limit: Int,
    ): List<AccountBootstrapSnapshot>
}
