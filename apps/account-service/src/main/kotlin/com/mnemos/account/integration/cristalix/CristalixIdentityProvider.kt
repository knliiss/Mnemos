package com.mnemos.account.integration.cristalix

import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.integration.cristalix.model.CristalixPlayer

interface CristalixIdentityProvider {
    fun findByNickname(nickname: CristalixNickname): CristalixPlayer?
}
