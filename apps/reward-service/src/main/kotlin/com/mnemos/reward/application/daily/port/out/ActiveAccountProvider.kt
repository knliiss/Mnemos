package com.mnemos.reward.application.daily.port.out

import com.mnemos.common.core.model.id.AccountId

fun interface ActiveAccountProvider {
    fun getActiveAccountId(): AccountId
}
