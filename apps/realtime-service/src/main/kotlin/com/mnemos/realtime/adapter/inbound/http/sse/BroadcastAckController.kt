package com.mnemos.realtime.adapter.inbound.http.sse

import com.mnemos.realtime.application.broadcast.usecase.acknowledge.AcknowledgeRealtimeBroadcastCommand
import com.mnemos.realtime.application.broadcast.usecase.acknowledge.AcknowledgeRealtimeBroadcastService
import com.mnemos.security.CurrentMnemosUser
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/realtime/broadcasts")
class BroadcastAckController(
    private val currentUser: CurrentMnemosUser,
    private val acknowledgeRealtimeBroadcastService: AcknowledgeRealtimeBroadcastService,
) {
    @PostMapping("/{sequence}/ack")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun acknowledge(
        @PathVariable
        sequence: Long,
    ) {
        acknowledgeRealtimeBroadcastService.execute(
            AcknowledgeRealtimeBroadcastCommand(
                userId = currentUser.requireUserId(),
                sequence = sequence,
            ),
        )
    }
}
