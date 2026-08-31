package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.account.AccountCallbackData
import com.mnemos.telegram.bot.application.account.model.AccountMenuFeedback
import com.mnemos.telegram.bot.application.account.model.AccountMenuFeedbackType
import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContext
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountInputMode
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
            is TelegramAccountContextResult.Available ->
                availableMessage(
                    context = result.context,
                    balance = accountData.data.balance,
                    feedback = accountData.data.feedback,
                    inputMode = accountData.data.inputMode,
                )

            TelegramAccountContextResult.RegistrationRequired ->
                registrationRequiredMessage(
                    feedback = accountData.data.feedback,
                    inputMode = accountData.data.inputMode,
                )

            TelegramAccountContextResult.Unavailable ->
                unavailableMessage(
                    feedback = accountData.data.feedback,
                    inputMode = accountData.data.inputMode,
                )
        }
    }

    private fun availableMessage(
        context: TelegramAccountContext,
        balance: TelegramAccountBalanceResult,
        feedback: AccountMenuFeedback?,
        inputMode: TelegramAccountInputMode?,
    ): OutboundTelegramMessage {
        val activeAccount = context.accounts.firstOrNull { account -> account.active }
        val lines =
            mutableListOf(
                "👤 <b>Аккаунт</b>",
                "",
                activeAccountLine(activeAccount),
                balanceLine(balance),
                "Доступно аккаунтов: <b>${context.accounts.size}</b>",
                "",
                accountLines(context),
            )

        appendInputPrompt(lines, inputMode)
        appendFeedback(lines, feedback)

        return OutboundTelegramMessage(
            text = lines.joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = availableActions(context, inputMode),
        )
    }

    private fun registrationRequiredMessage(
        feedback: AccountMenuFeedback?,
        inputMode: TelegramAccountInputMode?,
    ): OutboundTelegramMessage {
        val lines =
            mutableListOf(
                "👤 <b>Аккаунт</b>",
                "",
                "Cristalix-аккаунт ещё не подключён.",
                "Его можно полностью настроить прямо здесь, без Mini App.",
            )

        appendInputPrompt(lines, inputMode)
        appendFeedback(lines, feedback)

        return OutboundTelegramMessage(
            text = lines.joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = setupActions(inputMode) + navigationActions,
        )
    }

    private fun unavailableMessage(
        feedback: AccountMenuFeedback? = null,
        inputMode: TelegramAccountInputMode? = null,
    ): OutboundTelegramMessage {
        val lines =
            mutableListOf(
                "👤 <b>Аккаунт</b>",
                "",
                "Не удалось получить данные аккаунта. Попробуйте обновить через несколько секунд.",
            )

        appendInputPrompt(lines, inputMode)
        appendFeedback(lines, feedback)

        return OutboundTelegramMessage(
            text = lines.joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = setupActions(inputMode) + listOf(refreshAction) + navigationActions,
        )
    }

    private fun availableActions(
        context: TelegramAccountContext,
        inputMode: TelegramAccountInputMode?,
    ): List<OutboundTelegramMessageAction> {
        if (inputMode != null) {
            return setupActions(inputMode) + navigationActions
        }

        return accountActions(context) + setupActions(null) + listOf(refreshAction) + navigationActions
    }

    private fun activeAccountLine(account: TelegramAccountSummary?): String =
        account
            ?.let { activeAccount ->
                "Активный: <code>${escapeHtml(activeAccount.cristalixNickname)}</code>"
            } ?: "Активный: <b>не выбран</b>"

    private fun balanceLine(balance: TelegramAccountBalanceResult): String =
        when (balance) {
            is TelegramAccountBalanceResult.Available ->
                "Баланс: <b>${balance.balance.coin} COIN</b> · <b>${balance.balance.prime} PRIME</b>"

            TelegramAccountBalanceResult.NotRequired -> "Баланс: <b>—</b>"
            TelegramAccountBalanceResult.Unavailable -> "Баланс: <i>временно недоступен</i>"
        }

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
        val nickname = escapeHtml(account.cristalixNickname)
        val memberCount = "${account.memberCount}/${account.maxMembers}"

        return "$marker <code>$nickname</code> — $relation · $memberCount$status"
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

    private fun setupActions(inputMode: TelegramAccountInputMode?): List<OutboundTelegramMessageAction> =
        if (inputMode == null) {
            listOf(
                OutboundTelegramMessageAction.Callback(
                    label = "➕ Создать аккаунт",
                    data = AccountCallbackData.CREATE,
                ),
                OutboundTelegramMessageAction.Callback(
                    label = "🔗 Присоединиться",
                    data = AccountCallbackData.JOIN,
                ),
            )
        } else {
            listOf(
                OutboundTelegramMessageAction.Callback(
                    label = "✕ Отменить ввод",
                    data = AccountCallbackData.CANCEL_INPUT,
                ),
            )
        }

    private fun appendInputPrompt(
        lines: MutableList<String>,
        inputMode: TelegramAccountInputMode?,
    ) {
        val prompt =
            when (inputMode) {
                TelegramAccountInputMode.CREATE ->
                    "Отправьте <b>ник Cristalix</b> обычным сообщением. Бот создаст аккаунт и сделает его активным."

                TelegramAccountInputMode.JOIN ->
                    "Отправьте <b>код приглашения</b> обычным сообщением. После вступления аккаунт станет активным."

                null -> null
            } ?: return

        lines += ""
        lines += prompt
    }

    private fun appendFeedback(
        lines: MutableList<String>,
        feedback: AccountMenuFeedback?,
    ) {
        val feedbackText = renderFeedback(feedback) ?: return

        lines += ""
        lines += feedbackText
    }

    private fun renderFeedback(feedback: AccountMenuFeedback?): String? =
        when (feedback?.type) {
            AccountMenuFeedbackType.SWITCHED -> "✅ Активный аккаунт переключён."
            AccountMenuFeedbackType.CREATED -> "✅ Cristalix-аккаунт создан и выбран активным."
            AccountMenuFeedbackType.JOINED -> "✅ Вы присоединились к аккаунту, он выбран активным."
            AccountMenuFeedbackType.CREATE_INVALID ->
                "⚠️ Не удалось создать аккаунт. Проверьте ник Cristalix и отправьте его ещё раз."

            AccountMenuFeedbackType.JOIN_INVALID ->
                "⚠️ Код приглашения недействителен или больше недоступен. Отправьте другой код."

            AccountMenuFeedbackType.UNAVAILABLE ->
                "⚠️ Сервис временно недоступен. Ввод сохранён — можно повторить сообщение позже."

            null -> null
        }

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
        )
}
