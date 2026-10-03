package com.mnemos.notification.adapter.inbound.http.ping

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.ping.PingNotificationPreferenceService
import com.mnemos.security.CurrentMnemosUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/subscriptions/ping")
class PingNotificationPreferenceController(
    private val service: PingNotificationPreferenceService,
    private val currentMnemosUser: CurrentMnemosUser,
) {
    @GetMapping
    fun get(): PingNotificationPreferenceResponse {
        val userId = UserId(currentMnemosUser.requireUserId())
        return PingNotificationPreferenceResponse(enabled = service.isEnabled(userId))
    }

    @PatchMapping
    fun update(
        @RequestBody request: UpdatePingNotificationPreferenceRequest,
    ): PingNotificationPreferenceResponse {
        val userId = UserId(currentMnemosUser.requireUserId())
        service.update(userId, request.enabled)
        return PingNotificationPreferenceResponse(enabled = request.enabled)
    }
}

data class UpdatePingNotificationPreferenceRequest(
    val enabled: Boolean,
)

data class PingNotificationPreferenceResponse(
    val enabled: Boolean,
)
