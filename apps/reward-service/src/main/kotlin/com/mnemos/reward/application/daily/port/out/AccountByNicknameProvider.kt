package com.mnemos.reward.application.daily.port.out

import com.mnemos.common.core.model.id.AccountId

fun interface AccountByNicknameProvider {
    fun findAccountId(nickname: String): AccountId?
}
