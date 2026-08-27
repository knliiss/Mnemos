package com.mnemos.account.application.port.out

import com.mnemos.account.application.model.account.ResolvedPlayerIdentity
import com.mnemos.account.domain.model.account.CristalixNickname

fun interface PlayerIdentityProvider {
    fun resolveByNickname(nickname: CristalixNickname): ResolvedPlayerIdentity?
}
