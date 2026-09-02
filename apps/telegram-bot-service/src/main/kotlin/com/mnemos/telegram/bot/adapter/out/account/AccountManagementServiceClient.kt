package com.mnemos.telegram.bot.adapter.out.account

import com.mnemos.contracts.account.internal.v1.CreateInternalTelegramAccountRoleRequest
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountManagementResponse
import com.mnemos.contracts.account.internal.v1.MoveInternalTelegramAccountRoleRequest
import com.mnemos.contracts.account.internal.v1.SetInternalTelegramAccountRolePermissionRequest
import com.mnemos.contracts.account.internal.v1.UpdateInternalTelegramAccountRoleDetailsRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@FeignClient(
    name = "telegram-bot-account-management-service-client",
    url = "\${mnemos.clients.account-service.url}",
    configuration = [AccountServiceFeignConfiguration::class],
    dismiss404 = true,
)
interface AccountManagementServiceClient {
    @GetMapping("/internal/v1/users/telegram/{telegramId}/active-account/management")
    fun getManagement(
        @PathVariable telegramId: Long,
    ): InternalTelegramAccountManagementResponse?

    @DeleteMapping("/internal/v1/users/telegram/{telegramId}/active-account/management/members/{memberId}")
    fun kickMember(
        @PathVariable telegramId: Long,
        @PathVariable memberId: UUID,
    ): InternalTelegramAccountManagementResponse?

    @PutMapping("/internal/v1/users/telegram/{telegramId}/active-account/management/members/{memberId}/roles/{roleId}")
    fun assignRole(
        @PathVariable telegramId: Long,
        @PathVariable memberId: UUID,
        @PathVariable roleId: UUID,
    ): InternalTelegramAccountManagementResponse?

    @DeleteMapping("/internal/v1/users/telegram/{telegramId}/active-account/management/members/{memberId}/roles/{roleId}")
    fun revokeRole(
        @PathVariable telegramId: Long,
        @PathVariable memberId: UUID,
        @PathVariable roleId: UUID,
    ): InternalTelegramAccountManagementResponse?

    @PostMapping("/internal/v1/users/telegram/{telegramId}/active-account/management/roles")
    fun createRole(
        @PathVariable telegramId: Long,
        @RequestBody request: CreateInternalTelegramAccountRoleRequest,
    ): InternalTelegramAccountManagementResponse?

    @PatchMapping("/internal/v1/users/telegram/{telegramId}/active-account/management/roles/{roleId}")
    fun updateRoleDetails(
        @PathVariable telegramId: Long,
        @PathVariable roleId: UUID,
        @RequestBody request: UpdateInternalTelegramAccountRoleDetailsRequest,
    ): InternalTelegramAccountManagementResponse?

    @PatchMapping("/internal/v1/users/telegram/{telegramId}/active-account/management/roles/{roleId}/permission")
    fun setRolePermission(
        @PathVariable telegramId: Long,
        @PathVariable roleId: UUID,
        @RequestBody request: SetInternalTelegramAccountRolePermissionRequest,
    ): InternalTelegramAccountManagementResponse?

    @PutMapping("/internal/v1/users/telegram/{telegramId}/active-account/management/roles/{roleId}/position")
    fun moveRole(
        @PathVariable telegramId: Long,
        @PathVariable roleId: UUID,
        @RequestBody request: MoveInternalTelegramAccountRoleRequest,
    ): InternalTelegramAccountManagementResponse?

    @DeleteMapping("/internal/v1/users/telegram/{telegramId}/active-account/management/roles/{roleId}")
    fun deleteRole(
        @PathVariable telegramId: Long,
        @PathVariable roleId: UUID,
    ): InternalTelegramAccountManagementResponse?
}
