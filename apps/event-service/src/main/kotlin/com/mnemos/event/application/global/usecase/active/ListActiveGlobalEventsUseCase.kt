package com.mnemos.event.application.global.usecase.active

import com.mnemos.event.application.global.model.ActiveGlobalEventView

interface ListActiveGlobalEventsUseCase {
    fun execute(): List<ActiveGlobalEventView>
}
