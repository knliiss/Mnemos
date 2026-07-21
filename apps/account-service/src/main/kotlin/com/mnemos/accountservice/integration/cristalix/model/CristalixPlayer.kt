package com.mnemos.accountservice.integration.cristalix.model

import com.mnemos.accountservice.domain.model.account.CristalixNickname
import com.mnemos.accountservice.domain.model.account.CristalixUuid

data class CristalixPlayer(
    val uuid: CristalixUuid,
    val nickname: CristalixNickname,
)
