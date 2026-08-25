package com.mnemos.bet.application.event.port.out

import com.mnemos.bet.application.event.model.WipeWindow
import java.util.UUID

interface WipeProvider {
    fun current(): WipeWindow

    fun get(wipeId: UUID): WipeWindow
}
