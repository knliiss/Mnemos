package com.mnemos.personalization.adapter.inbound.http.badge

import com.mnemos.personalization.adapter.inbound.http.badge.BadgeApiMapper.toResponse
import com.mnemos.personalization.application.badge.usecase.activate.ActivateBadgeUseCase
import com.mnemos.personalization.application.badge.usecase.deactivate.DeactivateBadgeUseCase
import com.mnemos.personalization.application.badge.usecase.listmine.ListMyBadgesUseCase
import com.mnemos.personalization.domain.badge.model.BadgeId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/personalizations/badges")
class BadgeController(
    private val listMyBadgesUseCase: ListMyBadgesUseCase,
    private val activateBadgeUseCase: ActivateBadgeUseCase,
    private val deactivateBadgeUseCase: DeactivateBadgeUseCase,
) {
    @GetMapping("/me")
    fun listMine(): List<UserBadgeResponse> = listMyBadgesUseCase.execute().map { userBadge -> userBadge.toResponse() }

    @PutMapping("/{badgeId}/active")
    fun activate(
        @PathVariable badgeId: UUID,
    ): UserBadgeResponse =
        activateBadgeUseCase
            .execute(BadgeId(badgeId))
            .toResponse()

    @DeleteMapping("/active")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deactivateActive() {
        deactivateBadgeUseCase.execute()
    }
}
