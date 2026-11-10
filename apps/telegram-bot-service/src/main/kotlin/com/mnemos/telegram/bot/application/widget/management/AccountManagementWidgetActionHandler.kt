package com.mnemos.telegram.bot.application.widget.management

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.management.port.out.AccountManagementWidgetMutationGuard
import com.mnemos.telegram.bot.application.widget.management.port.out.AccountManagementWidgetPublisher
import com.mnemos.telegram.bot.application.widget.input.StartWidgetTextInputUseCase
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetActionHandler
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class AccountManagementWidgetActionHandler(
    private val snapshots: AccountManagementWidgetSnapshotQuery,
    private val publisher: AccountManagementWidgetPublisher,
    private val sessions: TelegramWidgetSessionRepository,
    private val responder: WidgetQueryResponder,
    private val guard: AccountManagementWidgetMutationGuard,
    private val mutations: ChangeAccountManagementWidgetUseCase,
    private val textInput: StartWidgetTextInputUseCase,
) : TelegramWidgetActionHandler {
    override val screen = TelegramWidgetScreen.ACCOUNT_MANAGEMENT

    override fun handle(context: TelegramWidgetQueryContext, session: TelegramWidgetSession): TelegramDeliveryResult {
        val current = when (val loaded = snapshots.load(context.actorId, session.page)) {
            is AccountManagementWidgetSnapshotResult.Available -> loaded.view
            AccountManagementWidgetSnapshotResult.AccountRequired ->
                return responder.reply(context.queryId, "ACCOUNT_REQUIRED", "Спочатку виберіть акаунт")
            AccountManagementWidgetSnapshotResult.Forbidden ->
                return responder.reply(context.queryId, "FORBIDDEN", "Немає дозволу на керування акаунтом")
            AccountManagementWidgetSnapshotResult.Unavailable ->
                return responder.reply(context.queryId, "UNAVAILABLE", "Керування акаунтом недоступне")
        }
        if (session.accountId != current.accountId) {
            return responder.reply(context.queryId, "CONTEXT_CHANGED", "Активний акаунт змінився")
        }

        val parts = context.data.split(':')
        if (parts.size >= 3 && parts[0] == "w1" && parts[1] == "manage") {
            when (parts[2]) {
                "create" -> {
                    if (parts.size != 3) return responder.reply(context.queryId, "INVALID", "Невірна дія")
                    if (!current.canManageRoles) {
                        return responder.reply(context.queryId, "FORBIDDEN", "Немає дозволу створювати ролі")
                    }
                    val ack = responder.reply(context.queryId, "INPUT", "Введіть назву ролі у відповідь на підказку")
                    if (ack == TelegramDeliveryResult.Delivered) {
                        runCatching { textInput.start(session, WidgetTextInputPurpose.ACCOUNT_ROLE_CREATE) }
                    }
                    return ack
                }
                "kick", "kick-confirm" -> {
                    if (parts.size != 6) return responder.reply(context.queryId, "INVALID", "Невірна дія")
                    val page = parts[3].toIntOrNull()
                    val index = parts[4].toIntOrNull()
                    if (page != session.page || index == null) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть сторінку")
                    }
                    val member = current.members.getOrNull(index)
                        ?: return responder.reply(context.queryId, "CONFLICT", "Учасника вже немає")
                    if (AccountManagementWidgetIdentity.tag(member.id) != parts[5]) {
                        return responder.reply(context.queryId, "CONFLICT", "Список учасників змінився. Оновіть")
                    }
                    if (!current.canRemoveMembers || member.owner || member.self) {
                        return responder.reply(context.queryId, "FORBIDDEN", "Немає дозволу виключити учасника")
                    }
                    if (parts[2] == "kick") {
                        guard.requestKick(context.actorId, context.messageId, current.accountId, member.id)
                        return responder.reply(
                            context.queryId,
                            "CONFIRM",
                            "Підтвердьте виключення учасника",
                            publisher.state(current.copy(pendingKickIndex = index)),
                        )
                    }
                    if (!guard.consumeKick(context.actorId, context.messageId, current.accountId, member.id)) {
                        return responder.reply(context.queryId, "CONFIRM_REQUIRED", "Підтвердьте виключення ще раз")
                    }
                    val result = mutations.kickMember(context.actorId, context.queryId, current, index)
                    if (result != AccountManagementWidgetMutationResult.APPLIED) return failure(context, result)
                    return refresh(context, session, current.accountId, 0, "Учасника виключено")
                }
                "role-delete", "role-delete-confirm" -> {
                    if (parts.size != 6) return responder.reply(context.queryId, "INVALID", "Невірна дія")
                    val page = parts[3].toIntOrNull()
                    val index = parts[4].toIntOrNull()
                    if (page != session.page || index == null) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть сторінку")
                    }
                    val role = current.roles.getOrNull(index)
                        ?: return responder.reply(context.queryId, "CONFLICT", "Роль більше недоступна")
                    if (AccountManagementWidgetIdentity.tag(role.id) != parts[5]) {
                        return responder.reply(context.queryId, "CONFLICT", "Список ролей змінився. Оновіть")
                    }
                    if (!current.canManageRoles) {
                        return responder.reply(context.queryId, "FORBIDDEN", "Немає дозволу видаляти ролі")
                    }
                    if (parts[2] == "role-delete") {
                        guard.requestRoleDeletion(context.actorId, context.messageId, current.accountId, role.id)
                        return responder.reply(
                            context.queryId,
                            "CONFIRM",
                            "Підтвердьте видалення ролі",
                            publisher.state(current.copy(pendingRoleDeletionIndex = index)),
                        )
                    }
                    if (!guard.consumeRoleDeletion(context.actorId, context.messageId, current.accountId, role.id)) {
                        return responder.reply(context.queryId, "CONFIRM_REQUIRED", "Підтвердьте видалення ролі ще раз")
                    }
                    val result = mutations.deleteRole(context.actorId, context.queryId, current, index)
                    if (result != AccountManagementWidgetMutationResult.APPLIED) return failure(context, result)
                    return refresh(context, session, current.accountId, session.page, "Роль видалено")
                }
                "role-delete-cancel" -> {
                    if (parts.size != 3) return responder.reply(context.queryId, "INVALID", "Невірна дія")
                    guard.cancelRoleDeletion(context.actorId, context.messageId)
                    return responder.reply(context.queryId, "OK", "Видалення скасовано", publisher.state(current))
                }
                "cancel" -> {
                    if (parts.size != 3) return responder.reply(context.queryId, "INVALID", "Невірна дія")
                    guard.cancelKick(context.actorId, context.messageId)
                    return responder.reply(context.queryId, "OK", "Виключення скасовано", publisher.state(current))
                }
                "edit" -> {
                    if (parts.size != 6) return responder.reply(context.queryId, "INVALID", "Невірна дія")
                    val page = parts[3].toIntOrNull()
                    val roleIndex = parts[4].toIntOrNull()
                    if (page != session.page || roleIndex == null) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть сторінку")
                    }
                    val role = current.roles.getOrNull(roleIndex)
                        ?: return responder.reply(context.queryId, "CONFLICT", "Роль більше недоступна")
                    if (AccountManagementWidgetIdentity.tag(role.id) != parts[5]) {
                        return responder.reply(context.queryId, "CONFLICT", "Список ролей змінився. Оновіть")
                    }
                    if (!current.canManageRoles) {
                        return responder.reply(context.queryId, "FORBIDDEN", "Немає дозволу редагувати ролі")
                    }
                    val ack = responder.reply(context.queryId, "INPUT", "Введіть назву та колір ролі у відповідь на підказку")
                    if (ack == TelegramDeliveryResult.Delivered) {
                        runCatching { textInput.startForRole(session, role.id) }
                    }
                    return ack
                }
                "permission" -> {
                    if (parts.size != 8) return responder.reply(context.queryId, "INVALID", "Невірна дія")
                    val page = parts[3].toIntOrNull()
                    val roleIndex = parts[4].toIntOrNull()
                    val permissionIndex = parts[5].toIntOrNull()
                    val enabled = when (parts[6]) {
                        "on" -> true
                        "off" -> false
                        else -> return responder.reply(context.queryId, "INVALID", "Невірна дія")
                    }
                    if (page != session.page || roleIndex == null || permissionIndex == null) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть сторінку")
                    }
                    val role = current.roles.getOrNull(roleIndex)
                    if (role == null || AccountManagementWidgetIdentity.tag(role.id) != parts[7]) {
                        return responder.reply(context.queryId, "CONFLICT", "Роль змінилася. Оновіть")
                    }
                    val result = mutations.setRolePermission(
                        context.actorId, context.queryId, current, roleIndex, permissionIndex, enabled,
                    )
                    if (result != AccountManagementWidgetMutationResult.APPLIED) return failure(context, result)
                    return refresh(context, session, current.accountId, session.page, "Дозволи ролі оновлено")
                }
                "move" -> {
                    if (parts.size != 8) return responder.reply(context.queryId, "INVALID", "Невірна дія")
                    val page = parts[3].toIntOrNull()
                    val roleIndex = parts[4].toIntOrNull()
                    val offset = when (parts[5]) {
                        "up" -> -1
                        "down" -> 1
                        else -> return responder.reply(context.queryId, "INVALID", "Невірний напрямок")
                    }
                    if (page != session.page || roleIndex == null) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть сторінку")
                    }
                    val role = current.roles.getOrNull(roleIndex)
                    val expectedRank = parts[7].toLongOrNull()
                    if (role == null || AccountManagementWidgetIdentity.tag(role.id) != parts[6] ||
                        expectedRank != role.rank
                    ) {
                        return responder.reply(context.queryId, "CONFLICT", "Порядок ролей змінився. Оновіть")
                    }
                    val result = mutations.moveRole(context.actorId, context.queryId, current, roleIndex, offset)
                    if (result != AccountManagementWidgetMutationResult.APPLIED) return failure(context, result)
                    return refresh(context, session, current.accountId, session.page, "Порядок ролей оновлено")
                }
                "role" -> {
                    if (parts.size != 9) return responder.reply(context.queryId, "INVALID", "Невірна дія")
                    val page = parts[3].toIntOrNull()
                    val memberIndex = parts[4].toIntOrNull()
                    val roleIndex = parts[5].toIntOrNull()
                    val enabled = when (parts[6]) {
                        "on" -> true
                        "off" -> false
                        else -> return responder.reply(context.queryId, "INVALID", "Невірна дія")
                    }
                    if (page != session.page || memberIndex == null || roleIndex == null) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть сторінку")
                    }
                    val member = current.members.getOrNull(memberIndex)
                    val role = current.roles.getOrNull(roleIndex)
                    if (member == null || role == null ||
                        AccountManagementWidgetIdentity.tag(member.id) != parts[7] ||
                        AccountManagementWidgetIdentity.tag(role.id) != parts[8]
                    ) {
                        return responder.reply(context.queryId, "CONFLICT", "Учасник або роль змінилися. Оновіть")
                    }
                    val result = mutations.assignRole(
                        context.actorId,
                        context.queryId,
                        current,
                        memberIndex,
                        roleIndex,
                        enabled,
                    )
                    if (result != AccountManagementWidgetMutationResult.APPLIED) return failure(context, result)
                    return refresh(context, session, current.accountId, session.page, "Ролі оновлено")
                }
            }
        }

        val targetPage =
            when {
                context.data == "w1:manage:refresh" -> session.page
                context.data.startsWith("w1:manage:next:") || context.data.startsWith("w1:manage:prev:") -> {
                    val expected = context.data.substringAfterLast(':').toIntOrNull()
                    if (expected != session.page) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть сторінку")
                    }
                    val next = if (context.data.startsWith("w1:manage:next:")) 1 else -1
                    (session.page + next).coerceIn(0, current.pages - 1)
                }
                else -> return responder.reply(context.queryId, "UNSUPPORTED", "Невідома дія")
            }
        guard.cancelKick(context.actorId, context.messageId)
        guard.cancelRoleDeletion(context.actorId, context.messageId)
        return refresh(context, session, current.accountId, targetPage, "Дані оновлено")
    }

    private fun refresh(
        context: TelegramWidgetQueryContext,
        session: TelegramWidgetSession,
        accountId: java.util.UUID,
        targetPage: Int,
        message: String,
    ): TelegramDeliveryResult {
        val fresh = (snapshots.load(context.actorId, targetPage) as? AccountManagementWidgetSnapshotResult.Available)?.view
            ?: return responder.reply(context.queryId, "UNAVAILABLE", "Дані зараз недоступні")
        if (fresh.accountId != accountId) {
            return responder.reply(context.queryId, "CONTEXT_CHANGED", "Активний акаунт змінився")
        }
        val answer = responder.reply(context.queryId, "OK", message, publisher.state(fresh))
        if (answer == TelegramDeliveryResult.Delivered) {
            sessions.save(session.copy(page = fresh.page))
            publisher.update(context.actorId, context.messageId, fresh)
        }
        return answer
    }

    private fun failure(
        context: TelegramWidgetQueryContext,
        result: AccountManagementWidgetMutationResult,
    ): TelegramDeliveryResult {
        val message = when (result) {
            AccountManagementWidgetMutationResult.APPLIED -> "Готово"
            AccountManagementWidgetMutationResult.ALREADY -> "Запит уже оброблено. Оновіть список"
            AccountManagementWidgetMutationResult.INVALID -> "Операцію відхилено. Оновіть список"
            AccountManagementWidgetMutationResult.FORBIDDEN -> "Недостатньо дозволів"
            AccountManagementWidgetMutationResult.ACCOUNT_REQUIRED -> "Виберіть активний акаунт"
            AccountManagementWidgetMutationResult.CONFLICT -> "Дані змінилися. Оновіть список"
            AccountManagementWidgetMutationResult.UNAVAILABLE -> "Стан операції невідомий. Перевірте список"
        }
        return responder.reply(context.queryId, result.name, message)
    }
}
