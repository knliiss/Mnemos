package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.mnemos.telegram.bot.application.prime.PrimePurchaseCallbackData
import com.mnemos.telegram.bot.application.prime.model.PrimeMenuScreenData
import com.mnemos.telegram.bot.application.prime.model.PrimePurchaseInvoice
import com.mnemos.telegram.bot.application.prime.model.PrimePurchaseNotice

object PrimeMenuMessageRenderer {
    fun render(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val primeData =
            (data as? TelegramMenuScreenData.Prime)?.data
                ?: return unavailableMessage()
        val rows = renderRows(primeData)

        return OutboundTelegramMessage(
            text = renderText(primeData),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )
    }

    private fun renderText(data: PrimeMenuScreenData): String {
        data.invoice?.let { invoice ->
            return readyInvoiceText(invoice)
        }

        val packages = data.packages ?: return unavailableText()
        val packageLines =
            packages
                .mapIndexed { index, primePackage ->
                    val marker = if (index + 1 == packages.size) "└" else "├"
                    "$marker <b>${primePackage.primeAmount} PRIME</b> · ⭐ ${primePackage.starsAmount}"
                }.joinToString("\n")
        val lines =
            mutableListOf(
                "⭐ <b>PRIME и баланс</b>",
                MESSAGE_DIVIDER,
                "",
                "💳 <b>Пополнение через Telegram Stars</b>",
                packageLines,
            )

        when (data.notice) {
            PrimePurchaseNotice.REGISTRATION_REQUIRED -> {
                lines += ""
                lines += "⚠️ Сначала настройте Cristalix-аккаунт."
            }

            PrimePurchaseNotice.UNAVAILABLE -> {
                lines += ""
                lines += "⚠️ Не удалось создать счёт. Попробуйте ещё раз позже."
            }

            null -> Unit
        }

        return lines.joinToString("\n")
    }

    private fun readyInvoiceText(invoice: PrimePurchaseInvoice): String =
        listOf(
            "⭐ <b>PRIME и баланс</b>",
            MESSAGE_DIVIDER,
            "",
            "⭐ <b>Счёт готов</b>",
            "└ ${invoice.primeAmount} PRIME · ⭐ ${invoice.starsAmount}",
            "",
            "Нажмите кнопку оплаты ниже.",
        ).joinToString("\n")

    private fun renderRows(data: PrimeMenuScreenData): List<List<OutboundTelegramMessageAction>> =
        buildList {
            data.invoice?.let { invoice ->
                add(
                    listOf(
                        OutboundTelegramMessageAction.OpenUrl(
                            label = "⭐ Оплатить ${invoice.primeAmount} PRIME",
                            url = invoice.url,
                            style = OutboundTelegramButtonStyle.SUCCESS,
                        ),
                    ),
                )
            }

            if (data.notice == PrimePurchaseNotice.REGISTRATION_REQUIRED) {
                add(
                    listOf(
                        OutboundTelegramMessageAction.Callback(
                            label = "👤 Аккаунт",
                            data = TelegramMenuScreen.ACCOUNT.callbackData,
                            style = OutboundTelegramButtonStyle.PRIMARY,
                        ),
                    ),
                )
            }

            if (data.invoice == null) {
                data.packages?.forEach { primePackage ->
                    add(
                        listOf(
                            OutboundTelegramMessageAction.Callback(
                                label = "⭐ ${primePackage.primeAmount} PRIME · ${primePackage.starsAmount} Stars",
                                data = PrimePurchaseCallbackData.buy(primePackage.id),
                                style = OutboundTelegramButtonStyle.PRIMARY,
                            ),
                        ),
                    )
                }
            }

            add(listOf(refreshAction))
            add(listOf(backAction))
        }

    private fun unavailableMessage(): OutboundTelegramMessage {
        val rows =
            listOf(
                listOf(refreshAction),
                listOf(backAction),
            )

        return OutboundTelegramMessage(
            text = unavailableText(),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )
    }

    private fun unavailableText(): String =
        listOf(
            "⭐ <b>PRIME и баланс</b>",
            MESSAGE_DIVIDER,
            "",
            "⚠️ Не удалось загрузить варианты пополнения.",
        ).joinToString("\n")

    private const val MESSAGE_DIVIDER = "━━━━━━━━━━━━━━━━━━━━"

    private val refreshAction =
        OutboundTelegramMessageAction.Callback(
            label = "🔄 Обновить",
            data = PrimePurchaseCallbackData.REFRESH,
        )

    private val backAction =
        OutboundTelegramMessageAction.Callback(
            label = "← Назад",
            data = TelegramMenuScreen.BACK_CALLBACK_DATA,
        )
}
