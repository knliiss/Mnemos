package com.mnemos.telegram.bot.adapter.out.settings

import com.mnemos.contracts.account.internal.v1.InternalTelegramLocalizationLanguage
import com.mnemos.contracts.account.internal.v1.UpdateInternalTelegramUserLocalizationRequest
import com.mnemos.personalization.contracts.internal.v1.UpdateInternalTelegramNotificationPackSelectionRequest
import com.mnemos.telegram.bot.adapter.out.account.AccountServiceClient
import com.mnemos.telegram.bot.application.settings.model.TelegramNotificationPackOption
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsMutationResult
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsSnapshot
import com.mnemos.telegram.bot.application.settings.port.out.SettingsGateway
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SettingsGatewayAdapter(
    private val accountClient: AccountServiceClient,
    private val personalizationClient: PersonalizationServiceClient,
) : SettingsGateway {
    override fun getSettings(telegramId: Long): TelegramSettingsSnapshot? {
        val userSettings = runSafely { accountClient.getUserSettings(telegramId) }
        val selection = runSafely { personalizationClient.getNotificationPackSelection(telegramId) }
        val packList = runSafely { personalizationClient.getNotificationPacks(telegramId) }

        if (userSettings == null && selection == null && packList == null) {
            return null
        }

        val packs =
            packList
                ?.packs
                .orEmpty()
                .map { pack ->
                    TelegramNotificationPackOption(
                        personalizationId = pack.personalizationId,
                        title = pack.name,
                        active = pack.active,
                    )
                }
        val activePack = packs.firstOrNull { pack -> pack.active }

        return TelegramSettingsSnapshot(
            localizationLanguage =
                userSettings?.localizationLanguage?.let { language ->
                    TelegramSettingsLanguage.valueOf(language.name)
                },
            activeNotificationPackId = selection?.personalizationId ?: activePack?.personalizationId,
            activeNotificationPackTitle = selection?.name ?: activePack?.title,
            notificationPacks = packs,
        )
    }

    override fun updateLanguage(
        telegramId: Long,
        language: TelegramSettingsLanguage,
    ): TelegramSettingsMutationResult =
        try {
            val response =
                accountClient.updateLocalization(
                    telegramId = telegramId,
                    request =
                        UpdateInternalTelegramUserLocalizationRequest(
                            localizationLanguage = InternalTelegramLocalizationLanguage.valueOf(language.name),
                        ),
                )

            if (response == null) {
                TelegramSettingsMutationResult.UNAVAILABLE
            } else {
                TelegramSettingsMutationResult.UPDATED
            }
        } catch (_: RuntimeException) {
            TelegramSettingsMutationResult.UNAVAILABLE
        }

    override fun activateNotificationPack(
        telegramId: Long,
        personalizationId: UUID,
    ): TelegramSettingsMutationResult =
        try {
            val response =
                personalizationClient.updateNotificationPackSelection(
                    telegramId = telegramId,
                    request = UpdateInternalTelegramNotificationPackSelectionRequest(personalizationId),
                )

            if (response == null) {
                TelegramSettingsMutationResult.UNAVAILABLE
            } else {
                TelegramSettingsMutationResult.UPDATED
            }
        } catch (_: RuntimeException) {
            TelegramSettingsMutationResult.UNAVAILABLE
        }

    private fun <T> runSafely(block: () -> T?): T? =
        try {
            block()
        } catch (_: RuntimeException) {
            null
        }
}
