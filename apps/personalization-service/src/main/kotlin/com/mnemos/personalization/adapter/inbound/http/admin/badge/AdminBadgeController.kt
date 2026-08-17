package com.mnemos.personalization.adapter.inbound.http.admin.badge

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.adapter.inbound.http.badge.BadgeApiMapper.toResponse
import com.mnemos.personalization.adapter.inbound.http.badge.BadgeResponse
import com.mnemos.personalization.application.badge.model.CreateBadgeCommand
import com.mnemos.personalization.application.badge.usecase.create.CreateBadgeUseCase
import com.mnemos.personalization.application.badge.usecase.grant.GrantBadgeUseCase
import com.mnemos.personalization.application.badge.usecase.revoke.RevokeBadgeUseCase
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.personalization.model.HexColor
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/personalizations/admin/badges")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
class AdminBadgeController(
    private val createBadgeUseCase: CreateBadgeUseCase,
    private val grantBadgeUseCase: GrantBadgeUseCase,
    private val revokeBadgeUseCase: RevokeBadgeUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody
        @Valid
        request: CreateBadgeRequest,
    ): BadgeResponse =
        createBadgeUseCase
            .execute(
                CreateBadgeCommand(
                    code = request.code,
                    name = request.name,
                    icon = request.icon,
                    color = HexColor(request.color),
                ),
            ).toResponse()

    @PostMapping("/{badgeId}/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun grant(
        @PathVariable badgeId: UUID,
        @PathVariable userId: UUID,
    ) {
        grantBadgeUseCase.execute(
            userId = UserId(userId),
            badgeId = BadgeId(badgeId),
        )
    }

    @DeleteMapping("/{badgeId}/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revoke(
        @PathVariable badgeId: UUID,
        @PathVariable userId: UUID,
    ) {
        revokeBadgeUseCase.execute(
            userId = UserId(userId),
            badgeId = BadgeId(badgeId),
        )
    }
}
