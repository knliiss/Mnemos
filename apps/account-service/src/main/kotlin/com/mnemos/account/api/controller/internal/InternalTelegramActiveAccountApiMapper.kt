package com.mnemos.account.api.controller.internal

import com.mnemos.account.application.query.user.InternalTelegramUserContext
import com.mnemos.contracts.account.internal.v1.InternalTelegramActiveAccountResponse

object InternalTelegramActiveAccountApiMapper {
    fun InternalTelegramUserContext.toActiveAccountResponse(): InternalTelegramActiveAccountResponse =
        InternalTelegramActiveAccountResponse(
            activeAccountId = activeAccountId?.id,
        )
}
