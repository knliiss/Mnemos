package com.mnemos.telegram.bot.application.widget.invites

import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteExpiry
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteMutationResult
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteStatus
import com.mnemos.telegram.bot.application.account.invite.port.out.AccountInviteGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.invites.port.out.InviteWidgetMutationGuard
import com.mnemos.telegram.bot.application.widget.invites.port.out.InviteWidgetPublisher
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetActionHandler
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class InviteWidgetActionHandler(
    private val snapshots: InviteWidgetSnapshotQuery,
    private val invites: AccountInviteGateway,
    private val publisher: InviteWidgetPublisher,
    private val sessions: TelegramWidgetSessionRepository,
    private val guard: InviteWidgetMutationGuard,
    private val responder: WidgetQueryResponder,
) : TelegramWidgetActionHandler {
    override val screen = TelegramWidgetScreen.INVITES

    override fun handle(context: TelegramWidgetQueryContext, session: TelegramWidgetSession): TelegramDeliveryResult {
        val current = when (val result = snapshots.load(context.actorId, session.page)) {
            is InviteWidgetSnapshotResult.Available -> result.view
            InviteWidgetSnapshotResult.AccountRequired ->
                return responder.reply(context.queryId, "ACCOUNT_REQUIRED", "Спочатку виберіть акаунт")
            InviteWidgetSnapshotResult.Forbidden ->
                return responder.reply(context.queryId, "FORBIDDEN", "Немає дозволу керувати запрошеннями")
            InviteWidgetSnapshotResult.Unavailable ->
                return responder.reply(context.queryId, "UNAVAILABLE", "Не вдалося завантажити запрошення")
        }
        if (current.accountId != session.accountId) {
            return responder.reply(context.queryId, "CONTEXT_CHANGED", "Акаунт змінився. Відкрийте /invites ще раз")
        }

        var page = current.page
        val message: String
        when {
            context.data == "w1:inv:refresh" -> message = "Список оновлено"
            context.data.startsWith("w1:inv:next:") || context.data.startsWith("w1:inv:prev:") -> {
                val forward = context.data.startsWith("w1:inv:next:")
                val expected = context.data.substringAfterLast(':').toIntOrNull()
                if (expected != session.page) {
                    return responder.reply(context.queryId, "CONFLICT", "Сторінку вже змінено")
                }
                page = (session.page + if (forward) 1 else -1).coerceIn(0, current.totalPages - 1)
                message = "Сторінку оновлено"
                guard.cancelRevocation(context.actorId, context.messageId)
            }
            context.data.startsWith("w1:inv:create:") -> {
                val expires = TelegramAccountInviteExpiry.entries.firstOrNull {
                    it.name == context.data.removePrefix("w1:inv:create:")
                } ?: return responder.reply(context.queryId, "INVALID", "Невірний термін дії")
                if (!guard.claimQuery(context.actorId, context.queryId)) {
                    return responder.reply(context.queryId, "ALREADY", "Запит уже оброблено. Оновіть список")
                }
                when (invites.createInvite(context.actorId, expires.expiresInMinutes)) {
                    TelegramAccountInviteMutationResult.UPDATED -> message = "Запрошення створено"
                    TelegramAccountInviteMutationResult.ACTIVE_ACCOUNT_REQUIRED ->
                        return responder.reply(context.queryId, "ACCOUNT_REQUIRED", "Спочатку виберіть акаунт")
                    TelegramAccountInviteMutationResult.FORBIDDEN ->
                        return responder.reply(context.queryId, "FORBIDDEN", "Немає дозволу створювати запрошення")
                    TelegramAccountInviteMutationResult.REFRESH ->
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть список запрошень")
                    TelegramAccountInviteMutationResult.UNAVAILABLE ->
                        return responder.reply(context.queryId, "UNAVAILABLE", "Стан операції невідомий. Перевірте список")
                }
            }
            context.data.startsWith("w1:inv:confirm:") -> {
                val id = parseInviteId(context.data, "w1:inv:confirm:")
                    ?: return responder.reply(context.queryId, "INVALID", "Невірне запрошення")
                if (current.invites.none { it.id == id && it.status == TelegramAccountInviteStatus.ACTIVE }) {
                    return responder.reply(context.queryId, "CONFLICT", "Запрошення більше не активне")
                }
                guard.requestRevocation(context.actorId, context.messageId, id)
                return responder.reply(
                    context.queryId,
                    "CONFIRM",
                    "Підтвердьте відкликання запрошення",
                    publisher.state(current.copy(pendingRevokeId = id)),
                )
            }
            context.data == "w1:inv:cancel" -> {
                guard.cancelRevocation(context.actorId, context.messageId)
                message = "Відкликання скасовано"
            }
            context.data.startsWith("w1:inv:revoke:") -> {
                val id = parseInviteId(context.data, "w1:inv:revoke:")
                    ?: return responder.reply(context.queryId, "INVALID", "Невірне запрошення")
                if (current.invites.none { it.id == id && it.status == TelegramAccountInviteStatus.ACTIVE }) {
                    return responder.reply(context.queryId, "CONFLICT", "Запрошення більше не активне")
                }
                if (!guard.consumeRevocation(context.actorId, context.messageId, id)) {
                    return responder.reply(context.queryId, "CONFIRM_REQUIRED", "Підтвердьте відкликання ще раз")
                }
                if (!guard.claimQuery(context.actorId, context.queryId)) {
                    return responder.reply(context.queryId, "ALREADY", "Запит уже оброблено. Оновіть список")
                }
                when (invites.revokeInvite(context.actorId, id)) {
                    TelegramAccountInviteMutationResult.UPDATED -> message = "Запрошення відкликано"
                    TelegramAccountInviteMutationResult.ACTIVE_ACCOUNT_REQUIRED ->
                        return responder.reply(context.queryId, "ACCOUNT_REQUIRED", "Спочатку виберіть акаунт")
                    TelegramAccountInviteMutationResult.FORBIDDEN ->
                        return responder.reply(context.queryId, "FORBIDDEN", "Немає дозволу відкликати запрошення")
                    TelegramAccountInviteMutationResult.REFRESH ->
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть список запрошень")
                    TelegramAccountInviteMutationResult.UNAVAILABLE ->
                        return responder.reply(context.queryId, "UNAVAILABLE", "Стан операції невідомий. Перевірте список")
                }
            }
            else -> return responder.reply(context.queryId, "UNSUPPORTED", "Невідома дія")
        }

        val fresh = (snapshots.load(context.actorId, page) as? InviteWidgetSnapshotResult.Available)?.view
            ?: return responder.reply(context.queryId, "OK", "$message. Оновіть віджет")
        if (fresh.accountId != session.accountId) {
            return responder.reply(context.queryId, "CONTEXT_CHANGED", "Активний акаунт змінився")
        }
        val response = responder.reply(context.queryId, "OK", message, publisher.state(fresh))
        if (response == TelegramDeliveryResult.Delivered) {
            sessions.save(session.copy(page = fresh.page))
            publisher.update(context.actorId, context.messageId, fresh)
        }
        return response
    }

    private fun parseInviteId(data: String, prefix: String): UUID? =
        runCatching { UUID.fromString(data.removePrefix(prefix)) }.getOrNull()
}
