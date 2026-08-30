package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.account.AccountCallbackData
import com.mnemos.telegram.bot.application.account.model.AccountMenuFeedback
import com.mnemos.telegram.bot.application.account.model.AccountMenuFeedbackType
import com.mnemos.telegram.bot.application.account.model.AccountMenuScreenData
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContext
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountStatus
import com.mnemos.telegram.bot.application.account.model.TelegramAccountSummary
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode

object AccountMenuMessageRenderer {
    fun render(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val accountData =
            data as? TelegramMenuScreenData.Account
                ?: return unavailableMessage()

        return when (val result = accountData.data.result) {
            is TelegramAccountContextResult.Available -> availableMessage(result.context, accountData.data.feedback)
            TelegramAccountContextResult.RegistrationRequired -> registrationRequiredMessage()
            TelegramAccountContextResult.Unavailable -> unavailableMessage()
        }
    }

    private fun availableMessage(
        context: TelegramAccountContext,
        feedback: AccountMenuFeedback?,
    ): OutboundTelegramMessage {
        val activeAccount = context.accounts.firstOrNull { account -> account.active }
        val lines =
            mutableListOf(
                "👤 <b>Аккаунт</b>",
                "",
                activeAccountLine(activeAccount),
                "Доступно аккаунтов: <b>${context.accounts.size}</b>",
            )

        lines += ""
        lines += accountLines(context)

        renderFeedback(feedback)?.let { feedbackText ->
            lines += ""
            lines += feedbackText
        }

        return OutboundTelegramMessage(
            text = lines.joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = accountActions(context) + refreshAction + navigationActions,
        )
    }

    private fun activeAccountLine(account: TelegramAccountSummary?): String =
        account
            ?.let { "Активный: <code>${escapeHtml(it.cristalixNickname)}</code>" }
            ?: "Активный: <b>не выбран</b>"

    private fun accountLines(context: TelegramAccountContext): String =
        context.accounts
            .joinToString("\n") { account -> renderAccountLine(account) }
            .ifBlank {
                "Cristalix-аккаунты пока не подключены."
            }

    private fun renderAccountLine(account: TelegramAccountSummary): String {
        val marker =
            when {
                account.active -> "✅"
                account.status == TelegramAccountStatus.DEACTIVATED -> "⛔"
                else -> "•"
            }
        val relation = if (account.owner) "владелец" else "участник"
        val status =
            if (account.status == TelegramAccountStatus.DEACTIVATED) {
                " · деактивирован"
            } else {
                ""
            }

        return "$marker <code>${escapeHtml(account.cristalixNickname)}</code> — $relation · ${account.memberCount}/${account.maxMembers}$status"
    }

    private fun accountActions(context: TelegramAccountContext): List<OutboundTelegramMessageAction> =
        context.accounts
            .filter { account -> !account.active && account.status == TelegramAccountStatus.ACTIVE }
            .map { account ->
                OutboundTelegramMessageAction.Callback(
                    label = "🔄 ${account.cristalixNickname}",
                    data = AccountCallbackData.switch(account.accountId),
                )
            }

    private fun renderFeedback(feedback: AccountMenuFeedback?): String? =
        when (feedback?.type) {
            AccountMenuFeedbackType.SWITCHED -> "✅ Активный аккаунт переключён."
            AccountMenuFeedbackType.UNAVAILABLE -> "⚠️ Не удалось переключить аккаунт. Попробуйте ещё раз."
            null -> null
        }

    private fun registrationRequiredMessage(): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text =
                listOf(
                    "👤 <b>Аккаунт</b>",
                    "",
                    "Аккаунт Mnemos ещё не настроен для этого Telegram-пользователя.",
                    "Откройте Mini App, чтобы создать или подключить Cristalix-аккаунт.",
                ).joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = navigationActions,
        )

    private fun unavailableMessage(): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text =
                listOf(
                    "👤 <b>Аккаунт</b>",
                    "",
                    "Не удалось получить данные аккаунта. Попробуйте обновить через несколько секунд.",
                ).joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = listOf(refreshAction) + navigationActions,
        )

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private val refreshAction =
        OutboundTelegramMessageAction.Callback(
            label = "🔄 Обновить",
            data = AccountCallbackData.REFRESH,
        )

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
