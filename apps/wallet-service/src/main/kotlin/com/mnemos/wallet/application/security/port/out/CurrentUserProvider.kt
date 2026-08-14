package com.mnemos.wallet.application.security.port.out

import com.mnemos.common.core.model.id.UserId

fun interface CurrentUserProvider {
    fun requireUserId(): UserId
}
