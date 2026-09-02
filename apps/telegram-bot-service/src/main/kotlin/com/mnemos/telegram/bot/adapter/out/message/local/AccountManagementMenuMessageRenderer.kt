package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.account.management.AccountManagementCallbackData
import com.mnemos.telegram.bot.application.account.management.model.AccountManagementConfirmation
import com.mnemos.telegram.bot.application.account.management.model.AccountManagementInputMode
import com.mnemos.telegram.bot.application.account.management.model.AccountManagementMenuFeedback
import com.mnemos.telegram.bot.application.account.management.model.AccountManagementMenuScreenData
import com.mnemos.telegram.bot.application.account.management.model.AccountManagementMenuView
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementMember
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementPermission
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementResult
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementSnapshot
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import java.util.UUID

object AccountManagementMenuMessageRenderer {
    fun render(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val screenData =
            (data as? TelegramMenuScreenData.AccountManagement)?.data
                ?: return unavailableMessage()

        return when (val result = screenData.result) {
            is TelegramAccountManagementResult.Available -> renderAvailable(result.snapshot, screenData)
            TelegramAccountManagementResult.ActiveAccountRequired -> activeAccountRequiredMessage()
            TelegramAccountManagementResult.Forbidden -> forbiddenMessage()
            TelegramAccountManagementResult.Unavailable -> unavailableMessage()
        }
    }

    private fun renderAvailable(
        snapshot: TelegramAccountManagementSnapshot,
        data: AccountManagementMenuScreenData,
    ): OutboundTelegramMessage {
        data.inputMode?.let { inputMode ->
            return roleInputMessage(snapshot, data, inputMode)
        }

        data.confirmation?.let { confirmation ->
            return confirmationMessage(snapshot, data, confirmation)
        }

        return when (data.view) {
            AccountManagementMenuView.OVERVIEW -> overviewMessage(snapshot, data.feedback)
            AccountManagementMenuView.MEMBERS -> membersMessage(snapshot, data.feedback)
            AccountManagementMenuView.MEMBER -> memberMessage(snapshot, data)
            AccountManagementMenuView.ROLES -> rolesMessage(snapshot, data.feedback)
            AccountManagementMenuView.ROLE -> roleMessage(snapshot, data)
        }
    }

    private fun overviewMessage(
        snapshot: TelegramAccountManagementSnapshot,
        feedback: AccountManagementMenuFeedback?,
    ): OutboundTelegramMessage {
        val lines =
            mutableListOf(
                "⚙️ <b>Управление аккаунтом</b>",
                TelegramMessageLayout.DIVIDER,
                "",
                "🔑 <code>${escapeHtml(snapshot.cristalixNickname)}</code>",
                "",
                "👥 Участники: <b>${snapshot.members.size}</b>",
                "🎭 Роли: <b>${snapshot.roles.size}</b>",
                "",
                "Участники, роли, права и приглашения активного Cristalix-аккаунта.",
            )

        appendFeedback(lines, feedback)

        return message(
            lines,
            listOf(
                listOf(membersAction),
                listOf(rolesAction),
                listOf(invitesAction),
                listOf(refreshAction),
                listOf(backAction),
            ),
        )
    }

    private fun membersMessage(
        snapshot: TelegramAccountManagementSnapshot,
        feedback: AccountManagementMenuFeedback?,
    ): OutboundTelegramMessage {
        val lines =
            mutableListOf(
                "👥 <b>Участники аккаунта</b>",
                TelegramMessageLayout.DIVIDER,
                "",
                "🔑 <code>${escapeHtml(snapshot.cristalixNickname)}</code>",
                "└ Всего: <b>${snapshot.members.size}</b>",
                "",
            )

        lines +=
            snapshot.members
                .mapIndexed { index, member -> memberLine(index, member, snapshot.actorMemberId) }
                .ifEmpty { listOf("Участников пока нет.") }
        appendFeedback(lines, feedback)

        val rows =
            snapshot.members.map { member ->
                listOf(
                    callback(
                        label = memberButtonLabel(member, snapshot.actorMemberId),
                        data = AccountManagementCallbackData.member(member.id),
                        style =
                            if (member.id == snapshot.actorMemberId) {
                                OutboundTelegramButtonStyle.SUCCESS
                            } else {
                                OutboundTelegramButtonStyle.DEFAULT
                            },
                    ),
                )
            } +
                listOf(
                    listOf(overviewAction),
                    listOf(refreshAction),
                    listOf(backAction),
                )

        return message(lines, rows)
    }

    private fun memberMessage(
        snapshot: TelegramAccountManagementSnapshot,
        data: AccountManagementMenuScreenData,
    ): OutboundTelegramMessage {
        val member =
            snapshot.members.firstOrNull { candidate -> candidate.id == data.selectedMemberId }
                ?: return membersMessage(snapshot, data.feedback)
        val assignedRoles = snapshot.roles.filter { role -> role.id in member.roleIds }
        val lines =
            mutableListOf(
                "👤 <b>Участник аккаунта</b>",
                TelegramMessageLayout.DIVIDER,
                "",
                "<b>${escapeHtml(member.name)}</b>${if (member.owner) " · владелец" else ""}",
                if (member.id == snapshot.actorMemberId) "└ Это вы" else "└ Участник",
                "",
                "🎭 <b>Назначенные роли</b>",
            )

        lines +=
            assignedRoles
                .map { role -> "• ${escapeHtml(role.name)} · <code>${escapeHtml(role.color)}</code>" }
                .ifEmpty { listOf("• Дополнительных ролей нет") }
        appendFeedback(lines, data.feedback)

        val rows =
            buildList {
                if (TelegramAccountManagementPermission.ASSIGN_MEMBER_ROLES in snapshot.actorPermissions) {
                    snapshot.roles.forEach { role ->
                        val assigned = role.id in member.roleIds

                        add(
                            listOf(
                                callback(
                                    label = if (assigned) "✓ ${role.name}" else "+ ${role.name}",
                                    data =
                                        if (assigned) {
                                            AccountManagementCallbackData.revokeRole(role.id)
                                        } else {
                                            AccountManagementCallbackData.assignRole(role.id)
                                        },
                                    style =
                                        if (assigned) {
                                            OutboundTelegramButtonStyle.SUCCESS
                                        } else {
                                            OutboundTelegramButtonStyle.PRIMARY
                                        },
                                ),
                            ),
                        )
                    }
                }

                if (canKick(snapshot, member)) {
                    add(
                        listOf(
                            callback(
                                label = "🗑 Исключить участника",
                                data = AccountManagementCallbackData.KICK_MEMBER,
                                style = OutboundTelegramButtonStyle.DANGER,
                            ),
                        ),
                    )
                }

                add(listOf(membersAction))
                add(listOf(overviewAction))
                add(listOf(backAction))
            }

        return message(lines, rows)
    }

    private fun rolesMessage(
        snapshot: TelegramAccountManagementSnapshot,
        feedback: AccountManagementMenuFeedback?,
    ): OutboundTelegramMessage {
        val lines =
            mutableListOf(
                "🎭 <b>Роли аккаунта</b>",
                TelegramMessageLayout.DIVIDER,
                "",
                "🔑 <code>${escapeHtml(snapshot.cristalixNickname)}</code>",
                "└ Ролей: <b>${snapshot.roles.size}</b>",
                "",
            )

        lines +=
            snapshot.roles
                .mapIndexed { index, role ->
                    "${index + 1}. <b>${escapeHtml(role.name)}</b> · <code>${escapeHtml(role.color)}</code> · ${role.permissions.size} прав"
                }.ifEmpty { listOf("Роли пока не настроены.") }
        appendFeedback(lines, feedback)

        val rows =
            buildList {
                snapshot.roles.forEach { role ->
                    add(
                        listOf(
                            callback(
                                label = "🎭 ${role.name}",
                                data = AccountManagementCallbackData.role(role.id),
                                style = OutboundTelegramButtonStyle.PRIMARY,
                            ),
                        ),
                    )
                }

                if (TelegramAccountManagementPermission.MANAGE_ROLES in snapshot.actorPermissions) {
                    add(
                        listOf(
                            callback(
                                label = "➕ Создать роль",
                                data = AccountManagementCallbackData.CREATE_ROLE,
                                style = OutboundTelegramButtonStyle.SUCCESS,
                            ),
                        ),
                    )
                }

                add(listOf(overviewAction))
                add(listOf(refreshAction))
                add(listOf(backAction))
            }

        return message(lines, rows)
    }

    private fun roleMessage(
        snapshot: TelegramAccountManagementSnapshot,
        data: AccountManagementMenuScreenData,
    ): OutboundTelegramMessage {
        val role =
            snapshot.roles.firstOrNull { candidate -> candidate.id == data.selectedRoleId }
                ?: return rolesMessage(snapshot, data.feedback)
        val index = snapshot.roles.indexOf(role)
        val lines =
            mutableListOf(
                "🎭 <b>${escapeHtml(role.name)}</b>",
                TelegramMessageLayout.DIVIDER,
                "",
                "🎨 Цвет: <code>${escapeHtml(role.color)}</code>",
                "↕️ Позиция: <b>${index + 1}/${snapshot.roles.size}</b>",
                "",
                "🔐 <b>Права роли</b>",
            )

        lines +=
            role.permissions
                .sortedBy { permission -> permission.name }
                .map { permission -> "✓ ${permissionLabel(permission)}" }
                .ifEmpty { listOf("• Дополнительных прав нет") }
        appendFeedback(lines, data.feedback)

        val rows =
            buildList {
                if (canManagePermissions(snapshot)) {
                    TelegramAccountManagementPermission.entries.forEach { permission ->
                        val enabled = permission in role.permissions

                        add(
                            listOf(
                                callback(
                                    label = "${if (enabled) "✓" else "○"} ${permissionLabel(permission)}",
                                    data = AccountManagementCallbackData.permission(permission, enabled = !enabled),
                                    style =
                                        if (enabled) {
                                            OutboundTelegramButtonStyle.SUCCESS
                                        } else {
                                            OutboundTelegramButtonStyle.DEFAULT
                                        },
                                ),
                            ),
                        )
                    }
                }

                if (TelegramAccountManagementPermission.MANAGE_ROLE_HIERARCHY in snapshot.actorPermissions) {
                    if (index > 0) {
                        add(
                            listOf(
                                callback(
                                    label = "⬆ Поднять роль",
                                    data = AccountManagementCallbackData.MOVE_ROLE_UP,
                                    style = OutboundTelegramButtonStyle.PRIMARY,
                                ),
                            ),
                        )
                    }

                    if (index in 0 until snapshot.roles.lastIndex) {
                        add(
                            listOf(
                                callback(
                                    label = "⬇ Опустить роль",
                                    data = AccountManagementCallbackData.MOVE_ROLE_DOWN,
                                    style = OutboundTelegramButtonStyle.PRIMARY,
                                ),
                            ),
                        )
                    }
                }

                if (TelegramAccountManagementPermission.MANAGE_ROLES in snapshot.actorPermissions) {
                    add(
                        listOf(
                            callback(
                                label = "✏️ Изменить имя и цвет",
                                data = AccountManagementCallbackData.EDIT_ROLE,
                                style = OutboundTelegramButtonStyle.PRIMARY,
                            ),
                        ),
                    )
                    add(
                        listOf(
                            callback(
                                label = "🗑 Удалить роль",
                                data = AccountManagementCallbackData.DELETE_ROLE,
                                style = OutboundTelegramButtonStyle.DANGER,
                            ),
                        ),
                    )
                }

                add(listOf(rolesAction))
                add(listOf(overviewAction))
                add(listOf(backAction))
            }

        return message(lines, rows)
    }

    private fun roleInputMessage(
        snapshot: TelegramAccountManagementSnapshot,
        data: AccountManagementMenuScreenData,
        inputMode: AccountManagementInputMode,
    ): OutboundTelegramMessage {
        val title =
            when (inputMode) {
                AccountManagementInputMode.CREATE_ROLE -> "➕ <b>Новая роль</b>"
                AccountManagementInputMode.EDIT_ROLE -> "✏️ <b>Изменение роли</b>"
            }
        val lines = mutableListOf(title, TelegramMessageLayout.DIVIDER, "")

        if (inputMode == AccountManagementInputMode.EDIT_ROLE) {
            snapshot.roles
                .firstOrNull { role -> role.id == data.selectedRoleId }
                ?.let { role ->
                    lines += "Сейчас: <b>${escapeHtml(role.name)}</b> · <code>${escapeHtml(role.color)}</code>"
                }
            lines += ""
        }

        lines += "Отправьте одним сообщением:"
        lines += "<code>Название роли #RRGGBB</code>"
        lines += ""
        lines += "Пример: <code>Модератор #7C5CFC</code>"
        appendFeedback(lines, data.feedback)

        return message(
            lines,
            listOf(
                listOf(
                    callback(
                        label = "✖ Отменить ввод",
                        data = AccountManagementCallbackData.CANCEL_INPUT,
                        style = OutboundTelegramButtonStyle.DANGER,
                    ),
                ),
            ),
        )
    }

    private fun confirmationMessage(
        snapshot: TelegramAccountManagementSnapshot,
        data: AccountManagementMenuScreenData,
        confirmation: AccountManagementConfirmation,
    ): OutboundTelegramMessage =
        when (confirmation) {
            AccountManagementConfirmation.KICK_MEMBER -> {
                val member =
                    snapshot.members.firstOrNull { candidate -> candidate.id == data.selectedMemberId }
                        ?: return memberMessage(snapshot, data.copy(confirmation = null))

                destructiveConfirmation(
                    title = "⚠️ <b>Исключить участника?</b>",
                    detail = "<b>${escapeHtml(member.name)}</b> потеряет доступ к аккаунту.",
                    confirmLabel = "🗑 Да, исключить",
                    confirmData = AccountManagementCallbackData.KICK_MEMBER_CONFIRM,
                )
            }

            AccountManagementConfirmation.DELETE_ROLE -> {
                val role =
                    snapshot.roles.firstOrNull { candidate -> candidate.id == data.selectedRoleId }
                        ?: return roleMessage(snapshot, data.copy(confirmation = null))

                destructiveConfirmation(
                    title = "⚠️ <b>Удалить роль?</b>",
                    detail = "Роль <b>${escapeHtml(role.name)}</b> будет удалена из аккаунта.",
                    confirmLabel = "🗑 Да, удалить роль",
                    confirmData = AccountManagementCallbackData.DELETE_ROLE_CONFIRM,
                )
            }
        }

    private fun destructiveConfirmation(
        title: String,
        detail: String,
        confirmLabel: String,
        confirmData: String,
    ): OutboundTelegramMessage =
        message(
            listOf(
                title,
                TelegramMessageLayout.DIVIDER,
                "",
                detail,
                "Это действие требует явного подтверждения.",
            ),
            listOf(
                listOf(
                    callback(
                        label = confirmLabel,
                        data = confirmData,
                        style = OutboundTelegramButtonStyle.DANGER,
                    ),
                ),
                listOf(cancelConfirmationAction),
            ),
        )

    private fun activeAccountRequiredMessage(): OutboundTelegramMessage =
        simpleStateMessage("⚠️ Сначала выберите активный Cristalix-аккаунт.")

    private fun forbiddenMessage(): OutboundTelegramMessage = simpleStateMessage("🔒 У вас нет доступа к управлению этим аккаунтом.")

    private fun unavailableMessage(): OutboundTelegramMessage =
        message(
            listOf(
                "⚙️ <b>Управление аккаунтом</b>",
                TelegramMessageLayout.DIVIDER,
                "",
                "⚠️ Не удалось получить данные управления аккаунтом.",
            ),
            listOf(listOf(refreshAction), listOf(backAction)),
        )

    private fun simpleStateMessage(detail: String): OutboundTelegramMessage =
        message(
            listOf(
                "⚙️ <b>Управление аккаунтом</b>",
                TelegramMessageLayout.DIVIDER,
                "",
                detail,
            ),
            listOf(listOf(backAction)),
        )

    private fun canKick(
        snapshot: TelegramAccountManagementSnapshot,
        member: TelegramAccountManagementMember,
    ): Boolean =
        TelegramAccountManagementPermission.MANAGE_MEMBERS in snapshot.actorPermissions &&
            !member.owner &&
            member.id != snapshot.actorMemberId

    private fun canManagePermissions(snapshot: TelegramAccountManagementSnapshot): Boolean =
        TelegramAccountManagementPermission.MANAGE_ROLES in snapshot.actorPermissions ||
            TelegramAccountManagementPermission.MANAGE_BASE_ROLE in snapshot.actorPermissions

    private fun memberLine(
        index: Int,
        member: TelegramAccountManagementMember,
        actorMemberId: UUID,
    ): String {
        val marker =
            when {
                member.owner -> "👑"
                member.id == actorMemberId -> "👤"
                else -> "•"
            }
        val role =
            member.highestRoleName
                ?.let(::escapeHtml)
                ?.let { name -> " · $name" }
                .orEmpty()

        return "${index + 1}. $marker <b>${escapeHtml(member.name)}</b>$role"
    }

    private fun memberButtonLabel(
        member: TelegramAccountManagementMember,
        actorMemberId: UUID,
    ): String =
        when {
            member.owner -> "👑 ${member.name}"
            member.id == actorMemberId -> "👤 ${member.name} · вы"
            else -> "👤 ${member.name}"
        }

    private fun appendFeedback(
        lines: MutableList<String>,
        feedback: AccountManagementMenuFeedback?,
    ) {
        val text =
            when (feedback) {
                AccountManagementMenuFeedback.UPDATED -> "✅ Изменения сохранены."
                AccountManagementMenuFeedback.INVALID -> "⚠️ Изменение отклонено. Проверьте данные или обновите экран."
                AccountManagementMenuFeedback.UNAVAILABLE -> "⚠️ Сервис временно недоступен. Попробуйте обновить экран."
                null -> return
            }

        lines += ""
        lines += text
    }

    private fun permissionLabel(permission: TelegramAccountManagementPermission): String =
        when (permission) {
            TelegramAccountManagementPermission.MANAGE_MEMBERS -> "Управление участниками"
            TelegramAccountManagementPermission.ASSIGN_MEMBER_ROLES -> "Назначение ролей"
            TelegramAccountManagementPermission.MANAGE_ROLES -> "Управление ролями"
            TelegramAccountManagementPermission.MANAGE_ROLE_HIERARCHY -> "Иерархия ролей"
            TelegramAccountManagementPermission.MANAGE_BASE_ROLE -> "Базовая роль"
            TelegramAccountManagementPermission.VIEW_TRANSACTION_HISTORY -> "История транзакций"
            TelegramAccountManagementPermission.PLACE_BETS -> "Ставки"
            TelegramAccountManagementPermission.BUY_ACCOUNT_PERSONALIZATIONS -> "Покупка персонализаций"
            TelegramAccountManagementPermission.APPLY_ACCOUNT_PERSONALIZATIONS -> "Применение персонализаций"
            TelegramAccountManagementPermission.MODERATE_COMMENTS -> "Модерация комментариев"
        }

    private fun message(
        lines: List<String>,
        rows: List<List<OutboundTelegramMessageAction>>,
    ): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = lines.joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )

    private fun callback(
        label: String,
        data: String,
        style: OutboundTelegramButtonStyle = OutboundTelegramButtonStyle.DEFAULT,
    ): OutboundTelegramMessageAction.Callback =
        OutboundTelegramMessageAction.Callback(
            label = label,
            data = data,
            style = style,
        )

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private val membersAction =
        callback(
            label = "👥 Участники",
            data = AccountManagementCallbackData.MEMBERS,
            style = OutboundTelegramButtonStyle.PRIMARY,
        )

    private val rolesAction =
        callback(
            label = "🎭 Роли и права",
            data = AccountManagementCallbackData.ROLES,
            style = OutboundTelegramButtonStyle.PRIMARY,
        )

    private val invitesAction =
        callback(
            label = "🔗 Приглашения",
            data = TelegramMenuScreen.ACCOUNT_INVITES.callbackData,
            style = OutboundTelegramButtonStyle.PRIMARY,
        )

    private val overviewAction = callback("⚙️ Обзор управления", AccountManagementCallbackData.OVERVIEW)
    private val refreshAction = callback("🔄 Обновить", AccountManagementCallbackData.REFRESH)
    private val cancelConfirmationAction = callback("✖ Отмена", AccountManagementCallbackData.CANCEL_CONFIRMATION)
    private val backAction = callback("← К аккаунту", TelegramMenuScreen.BACK_CALLBACK_DATA)
}
