package com.mnemos.account.integration.cristalix

import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.integration.cristalix.dto.CristalixPlayerResponse
import com.mnemos.account.integration.cristalix.model.CristalixPlayer

object CristalixPlayerMapper {
    fun CristalixPlayerResponse.toModel(): CristalixPlayer =
        CristalixPlayer(
            uuid = CristalixUuid(playerId),
            nickname = CristalixNickname(name),
        )
}
