package com.mnemos.account.api.controller.internal

import com.mnemos.account.application.query.user.InternalTelegramUserContext
import com.mnemos.contracts.account.internal.v1.InternalTelegramUserIdentityResponse

object InternalTelegramUserIdentityApiMapper {
    fun InternalTelegramUserContext.toIdentityResponse(): InternalTelegramUserIdentityResponse =
        InternalTelegramUserIdentityResponse(
            userId = userId.id,
        )
}
