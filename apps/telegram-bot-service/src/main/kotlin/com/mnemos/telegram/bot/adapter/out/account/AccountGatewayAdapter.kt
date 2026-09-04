package com.mnemos.telegram.bot.adapter.out.account

import com.mnemos.contracts.account.internal.v1.ChangeInternalTelegramActiveAccountRequest
import com.mnemos.contracts.account.internal.v1.CreateInternalTelegramAccountRequest
import com.mnemos.contracts.account.internal.v1.JoinInternalTelegramAccountRequest
import com.mnemos.telegram.bot.adapter.out.account.AccountApiMapper.toModel
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountMutationResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountSwitchResult
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import feign.FeignException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccountGatewayAdapter(
    private val client: AccountServiceClient,
) : AccountGateway {
    override fun getContext(telegramId: Long): TelegramAccountContextResult =
        try {
            client
                .getAccounts(telegramId)
                ?.let { response -> TelegramAccountContextResult.Available(response.toModel()) }
                ?: TelegramAccountContextResult.RegistrationRequired
        } catch (_: RuntimeException) {
            TelegramAccountContextResult.Unavailable
        }

    override fun createAccount(
        telegramId: Long,
        cristalixNickname: String,
    ): TelegramAccountMutationResult =
        mutate {
            client.createAccount(
                telegramId = telegramId,
                request = CreateInternalTelegramAccountRequest(cristalixNickname),
            )
        }

    override fun joinAccount(
        telegramId: Long,
        inviteCode: String,
    ): TelegramAccountMutationResult =
        mutate {
            client.joinAccount(
                telegramId = telegramId,
                request = JoinInternalTelegramAccountRequest(inviteCode),
            )
        }

    override fun changeActiveAccount(
        telegramId: Long,
        accountId: UUID,
    ): TelegramAccountSwitchResult =
        try {
            val response =
                client.changeActiveAccount(
                    telegramId = telegramId,
                    request = ChangeInternalTelegramActiveAccountRequest(accountId),
                )

            if (response == null) {
                TelegramAccountSwitchResult.RegistrationRequired
            } else {
                TelegramAccountSwitchResult.Switched
            }
        } catch (exception: FeignException) {
            if (exception.status() in 400..499) {
                TelegramAccountSwitchResult.Refresh
            } else {
                TelegramAccountSwitchResult.Unavailable
            }
        } catch (_: RuntimeException) {
            TelegramAccountSwitchResult.Unavailable
        }

    private fun mutate(action: () -> Any?): TelegramAccountMutationResult =
        try {
            if (action() == null) {
                TelegramAccountMutationResult.Invalid()
            } else {
                TelegramAccountMutationResult.Updated
            }
        } catch (exception: FeignException) {
            if (exception.status() in 400..499) {
                TelegramAccountMutationResult.Invalid(exception.errorCode())
            } else {
                TelegramAccountMutationResult.Unavailable
            }
        } catch (_: RuntimeException) {
            TelegramAccountMutationResult.Unavailable
        }

    private fun FeignException.errorCode(): String? =
        ERROR_CODE_PATTERN
            .find(contentUTF8())
            ?.groupValues
            ?.getOrNull(1)

    private companion object {
        val ERROR_CODE_PATTERN = Regex("\"code\"\\s*:\\s*\"([^\"]+)\"")
    }
}
