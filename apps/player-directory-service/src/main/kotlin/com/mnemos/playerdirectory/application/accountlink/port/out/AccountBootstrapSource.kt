package com.mnemos.playerdirectory.application.accountlink.port.out

import com.mnemos.playerdirectory.application.accountlink.model.AccountBootstrapSnapshot
import java.util.UUID

interface AccountBootstrapSource {
    fun fetch(
        afterAccountId: UUID?,
        limit: Int,
    ): List<AccountBootstrapSnapshot>
}
