package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.mnemos.telegram.bot.application.prime.PrimePurchaseCallbackData
import com.mnemos.telegram.bot.application.prime.model.PrimeMenuScreenData
import com.mnemos.telegram.bot.application.prime.model.PrimePurchaseNotice

object PrimeMenuMessageRenderer {
    fun render(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val primeData =
            (data as? TelegramMenuScreenData.Prime)?.data
                ?: return unavailableMessage()

        return OutboundTelegramMessage(
            text = renderText(primeData),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = renderActions(primeData),
        )
    }

    private fun renderText(data: PrimeMenuScreenData): String {
        val packages = data.packages ?: return unavailableText()
        val packageLines =
            packages.joinToString("\n") { primePackage ->
                "• <b>${primePackage.primeAmount} PRIME</b> — ⭐ ${primePackage.starsAmount}"
            }
        val lines =
            mutableListOf(
                "💎 <b>PRIME</b>",
                "",
                "Пополнение PRIME через Telegram Stars.",
                "",
                packageLines,
            )

        when (data.notice) {
            PrimePurchaseNotice.REGISTRATION_REQUIRED -> {
                lines += ""
                lines += "⚠️ Сначала зарегистрируйтесь в Mnemos через Mini App."
            }

            PrimePurchaseNotice.UNAVAILABLE -> {
                lines += ""
                lines += "⚠️ Не удалось создать счёт. Попробуйте ещё раз позже."
            }

            null -> Unit
        }

        data.invoice?.let { invoice ->
            lines += ""
            lines += "Счёт готов: <b>${invoice.primeAmount} PRIME</b> за ⭐ ${invoice.starsAmount}."
            lines += "Нажмите кнопку оплаты ниже."
        }

        return lines.joinToString("\n")
    }

    private fun renderActions(data: PrimeMenuScreenData): List<OutboundTelegramMessageAction> {
        val actions = mutableListOf<OutboundTelegramMessageAction>()

        data.invoice?.let { invoice ->
            actions +=
                OutboundTelegramMessageAction.OpenUrl(
                    label = "⭐ Оплатить ${invoice.primeAmount} PRIME",
                    url = invoice.url,
                )
        }

        data.packages?.forEach { primePackage ->
            actions +=
                OutboundTelegramMessageAction.Callback(
                    label = "${primePackage.primeAmount} PRIME · ⭐ ${primePackage.starsAmount}",
                    data = PrimePurchaseCallbackData.buy(primePackage.id),
                )
        }

        actions +=
            OutboundTelegramMessageAction.Callback(
                label = "🔄 Обновить",
                data = PrimePurchaseCallbackData.REFRESH,
            )
        actions += navigationActions

        return actions
    }

    private fun unavailableMessage(): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = unavailableText(),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions =
                listOf(
                    OutboundTelegramMessageAction.Callback(
                        label = "🔄 Обновить",
                        data = PrimePurchaseCallbackData.REFRESH,
                    ),
                ) + navigationActions,
        )

    private fun unavailableText(): String =
        listOf(
            "💎 <b>PRIME</b>",
            "",
            "Не удалось загрузить варианты пополнения. Попробуйте позже.",
        ).joinToString("\n")

    private val navigationActions =
        listOf(
            OutboundTelegramMessageAction.Callback(
                "← Назад",
                TelegramMenuScreen.BACK_CALLBACK_DATA,
            ),
            OutboundTelegramMessageAction.Callback(
                "⌂ Меню",
                TelegramMenuScreen.HOME.callbackData,
            ),
            OutboundTelegramMessageAction.OpenMiniApp("↗️ Открыть Mini App"),
        )
}
