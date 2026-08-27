package com.mnemos.playerdirectory.application.accountlink.port.out

import com.mnemos.playerdirectory.application.accountlink.model.PlayerAccountLink

interface PlayerAccountLinkRepository {
    fun save(link: PlayerAccountLink)
}
