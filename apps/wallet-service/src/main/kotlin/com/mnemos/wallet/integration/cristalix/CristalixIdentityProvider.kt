package com.mnemos.accountservice.integration.cristalix

import com.mnemos.accountservice.domain.model.account.CristalixNickname
import com.mnemos.accountservice.integration.cristalix.model.CristalixPlayer

interface CristalixIdentityProvider {
    fun findByNickname(nickname: CristalixNickname): CristalixPlayer?
}
