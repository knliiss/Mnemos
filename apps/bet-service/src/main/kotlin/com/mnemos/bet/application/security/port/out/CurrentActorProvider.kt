package com.mnemos.bet.application.security.port.out

import com.mnemos.common.core.model.id.UserId

interface CurrentActorProvider {
    fun requireUserId(): UserId
}
