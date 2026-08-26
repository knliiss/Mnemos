package com.mnemos.account.application.model.account

import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid

data class ResolvedPlayerIdentity(
    val uuid: CristalixUuid,
    val nickname: CristalixNickname,
)
