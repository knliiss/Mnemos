package com.mnemos.telegram.bot.adapter.out.account

import com.mnemos.contracts.account.internal.v1.CreateInternalTelegramAccountInviteRequest
import com.mnemos.telegram.bot.adapter.out.account.AccountInviteApiMapper.toModel
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteListResult
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteMutationResult
import com.mnemos.telegram.bot.application.account.invite.port.out.AccountInviteGateway
import feign.FeignException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccountInviteGatewayAdapter(
    private val client: AccountServiceClient,
) : AccountInviteGateway {
    override fun getInvites(telegramId: Long): TelegramAccountInviteListResult =
        try {
            client
                .getAccountInvites(telegramId)
                ?.let { response -> TelegramAccountInviteListResult.Available(response.toModel()) }
                ?: TelegramAccountInviteListResult.ActiveAccountRequired
        } catch (exception: FeignException) {
            when (exception.status()) {
                400, 404 -> TelegramAccountInviteListResult.ActiveAccountRequired
                403 -> TelegramAccountInviteListResult.Forbidden
                else -> TelegramAccountInviteListResult.Unavailable
            }
        } catch (_: RuntimeException) {
            TelegramAccountInviteListResult.Unavailable
        }

    override fun createInvite(
        telegramId: Long,
        expiresInMinutes: Long?,
    ): TelegramAccountInviteMutationResult =
        try {
            val response =
                client.createAccountInvite(
                    telegramId = telegramId,
                    request = CreateInternalTelegramAccountInviteRequest(expiresInMinutes),
                )

            if (response == null) {
                TelegramAccountInviteMutationResult.ACTIVE_ACCOUNT_REQUIRED
            } else {
                TelegramAccountInviteMutationResult.UPDATED
            }
        } catch (exception: FeignException) {
            mutationFailure(exception.status())
        } catch (_: RuntimeException) {
            TelegramAccountInviteMutationResult.UNAVAILABLE
        }

    override fun revokeInvite(
        telegramId: Long,
        inviteId: UUID,
    ): TelegramAccountInviteMutationResult =
        try {
            val response =
                client.revokeAccountInvite(
                    telegramId = telegramId,
                    inviteId = inviteId,
                )

            if (response == null) {
                TelegramAccountInviteMutationResult.REFRESH
            } else {
                TelegramAccountInviteMutationResult.UPDATED
            }
        } catch (exception: FeignException) {
            mutationFailure(exception.status())
        } catch (_: RuntimeException) {
            TelegramAccountInviteMutationResult.UNAVAILABLE
        }

    private fun mutationFailure(status: Int): TelegramAccountInviteMutationResult =
        when (status) {
            400 -> TelegramAccountInviteMutationResult.ACTIVE_ACCOUNT_REQUIRED
            403 -> TelegramAccountInviteMutationResult.FORBIDDEN
            in 400..499 -> TelegramAccountInviteMutationResult.REFRESH
            else -> TelegramAccountInviteMutationResult.UNAVAILABLE
        }
}
