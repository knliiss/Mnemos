package com.mnemos.account.api.controller.internal

import com.mnemos.account.application.query.user.InternalTelegramUserContext
import com.mnemos.contracts.account.internal.v1.InternalTelegramUserContextResponse

object InternalTelegramUserContextApiMapper {
    fun InternalTelegramUserContext.toResponse(): InternalTelegramUserContextResponse =
        InternalTelegramUserContextResponse(
            userId = userId.id,
            activeAccountId = activeAccountId?.id,
        )
}
