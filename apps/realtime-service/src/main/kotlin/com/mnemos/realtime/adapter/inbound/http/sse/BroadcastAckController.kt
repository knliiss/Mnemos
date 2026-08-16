package com.mnemos.realtime.adapter.inbound.http.sse

import com.mnemos.realtime.application.broadcast.port.out.RealtimeBroadcastReplayStore
import com.mnemos.security.CurrentMnemosUser
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/realtime/broadcasts")
class BroadcastAckController(
    private val currentUser: CurrentMnemosUser,
    private val replayStore: RealtimeBroadcastReplayStore,
) {
    @PostMapping("/{sequence}/ack")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun acknowledge(
        @PathVariable sequence: Long,
    ) {
        val userId = currentUser.requireUserId()
        if (!replayStore.acknowledge(userId, sequence)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Realtime broadcast was not found")
        }
    }
}
