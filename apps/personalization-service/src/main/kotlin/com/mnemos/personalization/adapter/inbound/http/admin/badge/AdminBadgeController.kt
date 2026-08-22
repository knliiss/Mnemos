package com.mnemos.personalization.adapter.inbound.http.admin.badge

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.adapter.inbound.http.badge.BadgeApiMapper.toResponse
import com.mnemos.personalization.adapter.inbound.http.badge.BadgeResponse
import com.mnemos.personalization.adapter.inbound.http.badge.UserBadgeResponse
import com.mnemos.personalization.application.badge.model.BadgeImageUpload
import com.mnemos.personalization.application.badge.model.CreateBadgeCommand
import com.mnemos.personalization.application.badge.usecase.create.CreateBadgeUseCase
import com.mnemos.personalization.application.badge.usecase.grant.GrantBadgeUseCase
import com.mnemos.personalization.application.badge.usecase.listall.ListBadgesUseCase
import com.mnemos.personalization.application.badge.usecase.listforuser.ListUserBadgesUseCase
import com.mnemos.personalization.application.badge.usecase.revoke.RevokeBadgeUseCase
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.personalization.model.HexColor
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/v1/personalizations/admin/badges")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
class AdminBadgeController(
    private val createBadgeUseCase: CreateBadgeUseCase,
    private val grantBadgeUseCase: GrantBadgeUseCase,
    private val revokeBadgeUseCase: RevokeBadgeUseCase,
    private val listBadgesUseCase: ListBadgesUseCase,
    private val listUserBadgesUseCase: ListUserBadgesUseCase,
) {
    @GetMapping
    fun list(): List<BadgeResponse> = listBadgesUseCase.execute().map { badge -> badge.toResponse() }

    @GetMapping("/users/{userId}")
    fun listForUser(
        @PathVariable userId: UUID,
    ): List<UserBadgeResponse> =
        listUserBadgesUseCase
            .execute(UserId(userId))
            .map { userBadge -> userBadge.toResponse() }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestPart("metadata")
        @Valid
        request: CreateBadgeRequest,
        @RequestPart("image") image: MultipartFile,
    ): BadgeResponse =
        createBadgeUseCase
            .execute(
                CreateBadgeCommand(
                    code = request.code,
                    name = request.name,
                    image =
                        BadgeImageUpload(
                            bytes = image.bytes,
                            contentType = image.contentType,
                        ),
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
