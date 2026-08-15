package com.mnemos.social.application.spotlight.port.out

import com.mnemos.social.domain.spotlight.model.DropSpotlight

interface DropSpotlightWallet {
    fun requestDebit(spotlight: DropSpotlight)
}
