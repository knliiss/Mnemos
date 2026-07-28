package com.mnemos.account.integration.cristalix.model

import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid

data class CristalixPlayer(
    val uuid: CristalixUuid,
    val nickname: CristalixNickname,
)
