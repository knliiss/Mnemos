package com.mnemos.personalization.adapter.inbound.http.internal.notificationpack

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.adapter.inbound.http.internal.notificationpack.InternalNotificationPackSelectionApiMapper.toResponse
import com.mnemos.personalization.application.ownership.usecase.getactivepack.GetActiveNotificationPackUseCase
import com.mnemos.personalization.contracts.internal.v1.InternalNotificationPackSelectionResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/users/{userId}/notification-pack-selection")
@PreAuthorize("authentication.name == 'social-service'")
class InternalNotificationPackSelectionController(
    private val getActiveNotificationPackUseCase: GetActiveNotificationPackUseCase,
) {
    @GetMapping
    fun get(
        @PathVariable userId: UUID,
    ): ResponseEntity<InternalNotificationPackSelectionResponse> {
        val selection =
            getActiveNotificationPackUseCase.execute(UserId(userId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(selection.toResponse())
    }
}
