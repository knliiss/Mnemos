package com.mnemos.telegram.bot.adapter.out.account

import com.mnemos.contracts.account.internal.v1.CreateInternalTelegramAccountRoleRequest
import com.mnemos.contracts.account.internal.v1.MoveInternalTelegramAccountRoleRequest
import com.mnemos.contracts.account.internal.v1.SetInternalTelegramAccountRolePermissionRequest
import com.mnemos.contracts.account.internal.v1.UpdateInternalTelegramAccountRoleDetailsRequest
import com.mnemos.telegram.bot.adapter.out.account.AccountManagementApiMapper.toContract
import com.mnemos.telegram.bot.adapter.out.account.AccountManagementApiMapper.toModel
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementMutationResult
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementPermission
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementResult
import com.mnemos.telegram.bot.application.account.management.port.out.AccountManagementGateway
import feign.FeignException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccountManagementGatewayAdapter(
    private val client: AccountManagementServiceClient,
) : AccountManagementGateway {
    override fun getManagement(telegramId: Long): TelegramAccountManagementResult =
        try {
            client
                .getManagement(telegramId)
                ?.let { response -> TelegramAccountManagementResult.Available(response.toModel()) }
                ?: TelegramAccountManagementResult.ActiveAccountRequired
        } catch (exception: FeignException) {
            when (exception.status()) {
                400, 404 -> TelegramAccountManagementResult.ActiveAccountRequired
                403 -> TelegramAccountManagementResult.Forbidden
                else -> TelegramAccountManagementResult.Unavailable
            }
        } catch (_: RuntimeException) {
            TelegramAccountManagementResult.Unavailable
        }

    override fun kickMember(
        telegramId: Long,
        memberId: UUID,
    ): TelegramAccountManagementMutationResult =
        mutate {
            client.kickMember(
                telegramId = telegramId,
                memberId = memberId,
            )
        }

    override fun setMemberRole(
        telegramId: Long,
        memberId: UUID,
        roleId: UUID,
        assigned: Boolean,
    ): TelegramAccountManagementMutationResult =
        mutate {
            if (assigned) {
                client.assignRole(
                    telegramId = telegramId,
                    memberId = memberId,
                    roleId = roleId,
                )
            } else {
                client.revokeRole(
                    telegramId = telegramId,
                    memberId = memberId,
                    roleId = roleId,
                )
            }
        }

    override fun createRole(
        telegramId: Long,
        name: String,
        color: String,
    ): TelegramAccountManagementMutationResult =
        mutate {
            client.createRole(
                telegramId = telegramId,
                request =
                    CreateInternalTelegramAccountRoleRequest(
                        name = name,
                        color = color,
                    ),
            )
        }

    override fun updateRoleDetails(
        telegramId: Long,
        roleId: UUID,
        name: String,
        color: String,
    ): TelegramAccountManagementMutationResult =
        mutate {
            client.updateRoleDetails(
                telegramId = telegramId,
                roleId = roleId,
                request =
                    UpdateInternalTelegramAccountRoleDetailsRequest(
                        name = name,
                        color = color,
                    ),
            )
        }

    override fun setRolePermission(
        telegramId: Long,
        roleId: UUID,
        permission: TelegramAccountManagementPermission,
        enabled: Boolean,
    ): TelegramAccountManagementMutationResult =
        mutate {
            client.setRolePermission(
                telegramId = telegramId,
                roleId = roleId,
                request =
                    SetInternalTelegramAccountRolePermissionRequest(
                        permission = permission.toContract(),
                        enabled = enabled,
                    ),
            )
        }

    override fun moveRole(
        telegramId: Long,
        roleId: UUID,
        targetIndex: Int,
    ): TelegramAccountManagementMutationResult =
        mutate {
            client.moveRole(
                telegramId = telegramId,
                roleId = roleId,
                request = MoveInternalTelegramAccountRoleRequest(targetIndex = targetIndex),
            )
        }

    override fun deleteRole(
        telegramId: Long,
        roleId: UUID,
    ): TelegramAccountManagementMutationResult =
        mutate {
            client.deleteRole(
                telegramId = telegramId,
                roleId = roleId,
            )
        }

    private fun mutate(operation: () -> Any?): TelegramAccountManagementMutationResult =
        try {
            if (operation() == null) {
                TelegramAccountManagementMutationResult.REFRESH
            } else {
                TelegramAccountManagementMutationResult.UPDATED
            }
        } catch (exception: FeignException) {
            when (exception.status()) {
                400, 409, 422 -> TelegramAccountManagementMutationResult.INVALID
                403 -> TelegramAccountManagementMutationResult.FORBIDDEN
                404 -> TelegramAccountManagementMutationResult.ACTIVE_ACCOUNT_REQUIRED
                in 400..499 -> TelegramAccountManagementMutationResult.REFRESH
                else -> TelegramAccountManagementMutationResult.UNAVAILABLE
            }
        } catch (_: RuntimeException) {
            TelegramAccountManagementMutationResult.UNAVAILABLE
        }
}
