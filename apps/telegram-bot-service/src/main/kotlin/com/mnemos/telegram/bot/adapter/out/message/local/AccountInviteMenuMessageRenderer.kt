package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.account.invite.AccountInviteCallbackData
import com.mnemos.telegram.bot.application.account.invite.model.AccountInviteMenuFeedback
import com.mnemos.telegram.bot.application.account.invite.model.AccountInviteMenuScreenData
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInvite
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteExpiry
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteListResult
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteStatus
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInvites
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object AccountInviteMenuMessageRenderer {
    fun render(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val inviteData =
            data as? TelegramMenuScreenData.AccountInvites
                ?: return unavailableMessage()

        return when (val result = inviteData.data.result) {
            is TelegramAccountInviteListResult.Available ->
                availableMessage(
                    data = inviteData.data,
                    snapshot = result.snapshot,
                )

            TelegramAccountInviteListResult.ActiveAccountRequired -> activeAccountRequiredMessage()
            TelegramAccountInviteListResult.Forbidden -> forbiddenMessage()
            TelegramAccountInviteListResult.Unavailable -> unavailableMessage(inviteData.data.feedback)
        }
    }

    private fun availableMessage(
        data: AccountInviteMenuScreenData,
        snapshot: TelegramAccountInvites,
    ): OutboundTelegramMessage {
        val revokeConfirmationId = data.revokeConfirmationId

        if (revokeConfirmationId != null) {
            return revokeConfirmationMessage(
                snapshot = snapshot,
                invite = snapshot.invites.firstOrNull { invite -> invite.id == revokeConfirmationId },
            )
        }

        val activeInvites = snapshot.invites.filter { invite -> invite.status == TelegramAccountInviteStatus.ACTIVE }
        val lines =
            mutableListOf(
                "🔗 <b>Приглашения</b>",
                TelegramMessageLayout.DIVIDER,
                "",
                "🔑 Аккаунт: <code>${escapeHtml(snapshot.cristalixNickname)}</code>",
                "🎟 Активных: <b>${activeInvites.size}</b>",
                "",
                "⏳ Новый инвайт: <b>${expiryText(data.expiry)}</b>",
                "Каждый код можно использовать один раз.",
            )

        feedbackText(data.feedback)?.let { feedback ->
            lines += ""
            lines += feedback
        }

        lines += ""
        lines += "<b>Активные коды</b>"
        lines += renderActiveInvites(activeInvites)

        val rows =
            buildList {
                add(expiryActions(data.expiry))
                add(listOf(createAction))
                addAll(revokeActions(activeInvites).chunked(4))
                add(listOf(refreshAction))
                add(listOf(backAction))
            }

        return message(
            text = lines.joinToString("\n"),
            rows = rows,
        )
    }

    private fun renderActiveInvites(invites: List<TelegramAccountInvite>): String =
        invites
            .mapIndexed { index, invite ->
                val expiry = invite.expiresAt?.let(inviteTimestampFormatter::format) ?: "без срока"

                "${index + 1}. <code>${escapeHtml(invite.code)}</code>\n└ $expiry"
            }.joinToString("\n\n")
            .ifBlank {
                "└ Активных приглашений нет."
            }

    private fun revokeConfirmationMessage(
        snapshot: TelegramAccountInvites,
        invite: TelegramAccountInvite?,
    ): OutboundTelegramMessage {
        if (invite == null || invite.status != TelegramAccountInviteStatus.ACTIVE) {
            return message(
                text =
                    listOf(
                        "🗑 <b>Отозвать приглашение</b>",
                        TelegramMessageLayout.DIVIDER,
                        "",
                        "Код уже недоступен или больше не активен.",
                    ).joinToString("\n"),
                rows = listOf(listOf(cancelRevokeAction)),
            )
        }

        val expiry = invite.expiresAt?.let(inviteTimestampFormatter::format) ?: "без срока"

        return message(
            text =
                listOf(
                    "🗑 <b>Отозвать приглашение?</b>",
                    TelegramMessageLayout.DIVIDER,
                    "",
                    "🔑 Аккаунт: <code>${escapeHtml(snapshot.cristalixNickname)}</code>",
                    "🎟 Код: <code>${escapeHtml(invite.code)}</code>",
                    "⏳ Срок: $expiry",
                    "",
                    "После отзыва этот код перестанет работать.",
                ).joinToString("\n"),
            rows =
                listOf(
                    listOf(
                        callback(
                            label = "🗑 Отозвать",
                            data = AccountInviteCallbackData.confirmRevoke(invite.id),
                            style = OutboundTelegramButtonStyle.DANGER,
                            accented = true,
                        ),
                    ),
                    listOf(cancelRevokeAction),
                ),
        )
    }

    private fun expiryActions(selected: TelegramAccountInviteExpiry): List<OutboundTelegramMessageAction> =
        listOf(
            expiryAction("∞", TelegramAccountInviteExpiry.NONE, selected),
            expiryAction("1ч", TelegramAccountInviteExpiry.ONE_HOUR, selected),
            expiryAction("1д", TelegramAccountInviteExpiry.ONE_DAY, selected),
            expiryAction("7д", TelegramAccountInviteExpiry.SEVEN_DAYS, selected),
        )

    private fun expiryAction(
        label: String,
        expiry: TelegramAccountInviteExpiry,
        selected: TelegramAccountInviteExpiry,
    ): OutboundTelegramMessageAction.Callback =
        callback(
            label = label,
            data = AccountInviteCallbackData.expiry(expiry),
            style =
                if (expiry == selected) {
                    OutboundTelegramButtonStyle.SUCCESS
                } else {
                    OutboundTelegramButtonStyle.DEFAULT
                },
        )

    private fun revokeActions(invites: List<TelegramAccountInvite>): List<OutboundTelegramMessageAction> =
        invites.mapIndexed { index, invite ->
            callback(
                label = "🗑 ${index + 1}",
                data = AccountInviteCallbackData.requestRevoke(invite.id),
                style = OutboundTelegramButtonStyle.DANGER,
            )
        }

    private fun activeAccountRequiredMessage(): OutboundTelegramMessage =
        message(
            text =
                listOf(
                    "🔗 <b>Приглашения</b>",
                    TelegramMessageLayout.DIVIDER,
                    "",
                    "Активный Cristalix-аккаунт не выбран.",
                    "Сначала выберите аккаунт в разделе «Аккаунты».",
                ).joinToString("\n"),
            rows = listOf(listOf(backAction)),
        )

    private fun forbiddenMessage(): OutboundTelegramMessage =
        message(
            text =
                listOf(
                    "🔗 <b>Приглашения</b>",
                    TelegramMessageLayout.DIVIDER,
                    "",
                    "⛔ Недостаточно прав для управления приглашениями активного аккаунта.",
                ).joinToString("\n"),
            rows =
                listOf(
                    listOf(refreshAction),
                    listOf(backAction),
                ),
        )

    private fun unavailableMessage(feedback: AccountInviteMenuFeedback? = null): OutboundTelegramMessage {
        val lines =
            mutableListOf(
                "🔗 <b>Приглашения</b>",
                TelegramMessageLayout.DIVIDER,
                "",
                "⚠️ Не удалось получить приглашения аккаунта.",
            )

        feedbackText(feedback)?.let { text ->
            lines += ""
            lines += text
        }

        return message(
            text = lines.joinToString("\n"),
            rows =
                listOf(
                    listOf(refreshAction),
                    listOf(backAction),
                ),
        )
    }

    private fun feedbackText(feedback: AccountInviteMenuFeedback?): String? =
        when (feedback) {
            AccountInviteMenuFeedback.CREATED -> "✅ Приглашение создано."
            AccountInviteMenuFeedback.REVOKED -> "✅ Приглашение отозвано."
            AccountInviteMenuFeedback.UNAVAILABLE -> "⚠️ Изменение не удалось применить. Попробуйте ещё раз."
            AccountInviteMenuFeedback.REFRESHED -> null
            null -> null
        }

    private fun expiryText(expiry: TelegramAccountInviteExpiry): String =
        when (expiry) {
            TelegramAccountInviteExpiry.NONE -> "без срока"
            TelegramAccountInviteExpiry.ONE_HOUR -> "1 час"
            TelegramAccountInviteExpiry.ONE_DAY -> "1 день"
            TelegramAccountInviteExpiry.SEVEN_DAYS -> "7 дней"
        }

    private fun message(
        text: String,
        rows: List<List<OutboundTelegramMessageAction>>,
    ): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = text,
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )

    private fun callback(
        label: String,
        data: String,
        style: OutboundTelegramButtonStyle = OutboundTelegramButtonStyle.DEFAULT,
        accented: Boolean = false,
    ): OutboundTelegramMessageAction.Callback =
        OutboundTelegramMessageAction.Callback(
            label = label,
            data = data,
            style = style,
            accented = accented,
        )

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private val createAction =
        callback(
            label = "➕ Создать приглашение",
            data = AccountInviteCallbackData.CREATE,
            style = OutboundTelegramButtonStyle.PRIMARY,
        )

    private val refreshAction = callback("🔄 Обновить", AccountInviteCallbackData.REFRESH)

    private val backAction = callback("← Назад", TelegramMenuScreen.BACK_CALLBACK_DATA)

    private val cancelRevokeAction =
        callback(
            label = "← Не отзывать",
            data = AccountInviteCallbackData.REVOKE_CANCEL,
        )

    private val inviteTimestampFormatter: DateTimeFormatter =
        DateTimeFormatter
            .ofPattern("dd.MM HH:mm 'UTC'")
            .withZone(ZoneOffset.UTC)
}
