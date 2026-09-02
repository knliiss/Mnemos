package com.mnemos.account.api.controller.internal

import com.mnemos.account.api.controller.internal.InternalTelegramAccountInviteApiMapper.toResponse
import com.mnemos.account.application.service.invite.InternalTelegramAccountInviteService
import com.mnemos.account.domain.model.account.invite.InviteId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.contracts.account.internal.v1.CreateInternalTelegramAccountInviteRequest
import com.mnemos.contracts.account.internal.v1.CreateInternalTelegramAccountInviteResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountInvitesResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/users/telegram/{telegramId}/active-account/invites")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class InternalTelegramAccountInviteController(
    private val service: InternalTelegramAccountInviteService,
) {
    @GetMapping
    fun getInvites(
        @PathVariable telegramId: Long,
    ): ResponseEntity<InternalTelegramAccountInvitesResponse> {
        val invites =
            service.getInvites(TelegramId(telegramId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(invites.toResponse())
    }

    @PostMapping
    fun createInvite(
        @PathVariable telegramId: Long,
        @RequestBody request: CreateInternalTelegramAccountInviteRequest,
    ): ResponseEntity<CreateInternalTelegramAccountInviteResponse> {
        val invite =
            service.createInvite(
                telegramId = TelegramId(telegramId),
                expiresInMinutes = request.expiresInMinutes,
            ) ?: return ResponseEntity.notFound().build()

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(invite.toResponse())
    }

    @DeleteMapping("/{inviteId}")
    fun revokeInvite(
        @PathVariable telegramId: Long,
        @PathVariable inviteId: UUID,
    ): ResponseEntity<Void> {
        service.revokeInvite(
            telegramId = TelegramId(telegramId),
            inviteId = InviteId(inviteId),
        ) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.noContent().build()
    }
}
