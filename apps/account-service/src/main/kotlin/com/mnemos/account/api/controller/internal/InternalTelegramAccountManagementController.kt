package com.mnemos.account.api.controller.internal

import com.mnemos.account.api.controller.internal.InternalTelegramAccountManagementApiMapper.toDomain
import com.mnemos.account.api.controller.internal.InternalTelegramAccountManagementApiMapper.toResponse
import com.mnemos.account.application.service.account.InternalTelegramAccountManagement
import com.mnemos.account.application.service.account.InternalTelegramAccountManagementService
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.contracts.account.internal.v1.CreateInternalTelegramAccountRoleRequest
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountManagementResponse
import com.mnemos.contracts.account.internal.v1.MoveInternalTelegramAccountRoleRequest
import com.mnemos.contracts.account.internal.v1.SetInternalTelegramAccountRolePermissionRequest
import com.mnemos.contracts.account.internal.v1.UpdateInternalTelegramAccountRoleDetailsRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/users/telegram/{telegramId}/active-account/management")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class InternalTelegramAccountManagementController(
    private val service: InternalTelegramAccountManagementService,
) {
    @GetMapping
    fun getManagement(
        @PathVariable telegramId: Long,
    ): ResponseEntity<InternalTelegramAccountManagementResponse> = response(service.getManagement(TelegramId(telegramId)))

    @DeleteMapping("/members/{memberId}")
    fun kickMember(
        @PathVariable telegramId: Long,
        @PathVariable memberId: UUID,
    ): ResponseEntity<InternalTelegramAccountManagementResponse> =
        response(
            service.kickMember(
                telegramId = TelegramId(telegramId),
                memberId = AccountMemberId(memberId),
            ),
        )

    @PutMapping("/members/{memberId}/roles/{roleId}")
    fun assignRole(
        @PathVariable telegramId: Long,
        @PathVariable memberId: UUID,
        @PathVariable roleId: UUID,
    ): ResponseEntity<InternalTelegramAccountManagementResponse> =
        response(
            service.setMemberRole(
                telegramId = TelegramId(telegramId),
                memberId = AccountMemberId(memberId),
                roleId = RoleId(roleId),
                assigned = true,
            ),
        )

    @DeleteMapping("/members/{memberId}/roles/{roleId}")
    fun revokeRole(
        @PathVariable telegramId: Long,
        @PathVariable memberId: UUID,
        @PathVariable roleId: UUID,
    ): ResponseEntity<InternalTelegramAccountManagementResponse> =
        response(
            service.setMemberRole(
                telegramId = TelegramId(telegramId),
                memberId = AccountMemberId(memberId),
                roleId = RoleId(roleId),
                assigned = false,
            ),
        )

    @PostMapping("/roles")
    fun createRole(
        @PathVariable telegramId: Long,
        @RequestBody request: CreateInternalTelegramAccountRoleRequest,
    ): ResponseEntity<InternalTelegramAccountManagementResponse> {
        val management =
            service.createRole(
                telegramId = TelegramId(telegramId),
                name = request.name,
                color = request.color,
            ) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.status(HttpStatus.CREATED).body(management.toResponse())
    }

    @PatchMapping("/roles/{roleId}")
    fun updateRoleDetails(
        @PathVariable telegramId: Long,
        @PathVariable roleId: UUID,
        @RequestBody request: UpdateInternalTelegramAccountRoleDetailsRequest,
    ): ResponseEntity<InternalTelegramAccountManagementResponse> =
        response(
            service.updateRoleDetails(
                telegramId = TelegramId(telegramId),
                roleId = RoleId(roleId),
                name = request.name,
                color = request.color,
            ),
        )

    @PatchMapping("/roles/{roleId}/permission")
    fun setRolePermission(
        @PathVariable telegramId: Long,
        @PathVariable roleId: UUID,
        @RequestBody request: SetInternalTelegramAccountRolePermissionRequest,
    ): ResponseEntity<InternalTelegramAccountManagementResponse> =
        response(
            service.setRolePermission(
                telegramId = TelegramId(telegramId),
                roleId = RoleId(roleId),
                permission = request.permission.toDomain(),
                enabled = request.enabled,
            ),
        )

    @PutMapping("/roles/{roleId}/position")
    fun moveRole(
        @PathVariable telegramId: Long,
        @PathVariable roleId: UUID,
        @RequestBody request: MoveInternalTelegramAccountRoleRequest,
    ): ResponseEntity<InternalTelegramAccountManagementResponse> =
        response(
            service.moveRole(
                telegramId = TelegramId(telegramId),
                roleId = RoleId(roleId),
                targetIndex = request.targetIndex,
            ),
        )

    @DeleteMapping("/roles/{roleId}")
    fun deleteRole(
        @PathVariable telegramId: Long,
        @PathVariable roleId: UUID,
    ): ResponseEntity<InternalTelegramAccountManagementResponse> =
        response(
            service.deleteRole(
                telegramId = TelegramId(telegramId),
                roleId = RoleId(roleId),
            ),
        )

    private fun response(management: InternalTelegramAccountManagement?): ResponseEntity<InternalTelegramAccountManagementResponse> =
        management
            ?.toResponse()
            ?.let(ResponseEntity::ok)
            ?: ResponseEntity.notFound().build()
}
