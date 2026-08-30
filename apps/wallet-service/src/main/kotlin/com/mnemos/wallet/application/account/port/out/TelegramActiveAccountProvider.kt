package com.mnemos.wallet.application.account.port.out

import com.mnemos.common.core.model.id.AccountId

fun interface TelegramActiveAccountProvider {
    fun findActiveAccountId(telegramId: Long): AccountId?
}
