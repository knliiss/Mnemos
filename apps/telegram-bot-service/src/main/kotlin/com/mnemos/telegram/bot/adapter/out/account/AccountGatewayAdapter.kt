package com.mnemos.telegram.bot.adapter.out.account

import com.mnemos.contracts.account.internal.v1.ChangeInternalTelegramActiveAccountRequest
import com.mnemos.telegram.bot.adapter.out.account.AccountApiMapper.toModel
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
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

    override fun changeActiveAccount(
        telegramId: Long,
        accountId: UUID,
    ): TelegramAccountSwitchResult =
        try {
            client
                .changeActiveAccount(
                    telegramId = telegramId,
                    request = ChangeInternalTelegramActiveAccountRequest(accountId),
                )
                ?.let { TelegramAccountSwitchResult.Switched }
                ?: TelegramAccountSwitchResult.RegistrationRequired
        } catch (exception: FeignException) {
            if (exception.status() in 400..499) {
                TelegramAccountSwitchResult.Refresh
            } else {
                TelegramAccountSwitchResult.Unavailable
            }
        } catch (_: RuntimeException) {
            TelegramAccountSwitchResult.Unavailable
        }
}
