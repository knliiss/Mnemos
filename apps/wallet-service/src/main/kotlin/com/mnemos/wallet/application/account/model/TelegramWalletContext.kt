package com.mnemos.wallet.application.account.model

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId

data class TelegramWalletContext(
    val userId: UserId,
    val activeAccountId: AccountId?,
)
