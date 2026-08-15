package com.mnemos.realtime.application.delivery.usecase.publish

import com.mnemos.realtime.application.delivery.model.RealtimeEvent

interface PublishRealtimeEventUseCase {
    fun execute(command: PublishRealtimeEventCommand): RealtimeEvent
}
