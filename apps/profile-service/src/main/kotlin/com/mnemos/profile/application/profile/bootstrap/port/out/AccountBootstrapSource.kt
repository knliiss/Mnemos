package com.mnemos.profile.application.profile.bootstrap.port.out

import com.mnemos.profile.application.profile.bootstrap.model.AccountBootstrapSnapshot
import java.util.UUID

interface AccountBootstrapSource {
    fun fetch(
        afterAccountId: UUID?,
        limit: Int,
    ): List<AccountBootstrapSnapshot>
}
