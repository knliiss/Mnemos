package com.mnemos.telegram.bot.application.account.management

import com.mnemos.telegram.bot.application.account.management.model.AccountManagementConfirmation
import com.mnemos.telegram.bot.application.account.management.model.AccountManagementMenuFeedback
import com.mnemos.telegram.bot.application.account.management.model.AccountManagementMenuView
import java.util.UUID

object AccountManagementMenuContext {
    private const val VIEW_KEY = "account.management.view"
    private const val MEMBER_ID_KEY = "account.management.member-id"
    private const val ROLE_ID_KEY = "account.management.role-id"
    private const val FEEDBACK_KEY = "account.management.feedback"
    private const val CONFIRMATION_KEY = "account.management.confirmation"

    fun view(context: Map<String, String>): AccountManagementMenuView =
        enumValueOrNull<AccountManagementMenuView>(context[VIEW_KEY])
            ?: AccountManagementMenuView.OVERVIEW

    fun selectedMemberId(context: Map<String, String>): UUID? = uuidOrNull(context[MEMBER_ID_KEY])

    fun selectedRoleId(context: Map<String, String>): UUID? = uuidOrNull(context[ROLE_ID_KEY])

    fun feedback(context: Map<String, String>): AccountManagementMenuFeedback? = enumValueOrNull(context[FEEDBACK_KEY])

    fun confirmation(context: Map<String, String>): AccountManagementConfirmation? = enumValueOrNull(context[CONFIRMATION_KEY])

    fun showOverview(context: Map<String, String>): Map<String, String> =
        context
            .withoutTransientState()
            .minus(MEMBER_ID_KEY)
            .minus(ROLE_ID_KEY) +
            (VIEW_KEY to AccountManagementMenuView.OVERVIEW.name)

    fun showMembers(context: Map<String, String>): Map<String, String> =
        context
            .withoutTransientState()
            .minus(MEMBER_ID_KEY)
            .minus(ROLE_ID_KEY) +
            (VIEW_KEY to AccountManagementMenuView.MEMBERS.name)

    fun showMember(
        context: Map<String, String>,
        memberId: UUID,
    ): Map<String, String> =
        context
            .withoutTransientState()
            .minus(ROLE_ID_KEY) +
            mapOf(
                VIEW_KEY to AccountManagementMenuView.MEMBER.name,
                MEMBER_ID_KEY to memberId.toString(),
            )

    fun showRoles(context: Map<String, String>): Map<String, String> =
        context
            .withoutTransientState()
            .minus(MEMBER_ID_KEY)
            .minus(ROLE_ID_KEY) +
            (VIEW_KEY to AccountManagementMenuView.ROLES.name)

    fun showRole(
        context: Map<String, String>,
        roleId: UUID,
    ): Map<String, String> =
        context
            .withoutTransientState()
            .minus(MEMBER_ID_KEY) +
            mapOf(
                VIEW_KEY to AccountManagementMenuView.ROLE.name,
                ROLE_ID_KEY to roleId.toString(),
            )

    fun requestKick(context: Map<String, String>): Map<String, String> =
        context
            .withoutTransientState() +
            (CONFIRMATION_KEY to AccountManagementConfirmation.KICK_MEMBER.name)

    fun requestRoleDelete(context: Map<String, String>): Map<String, String> =
        context
            .withoutTransientState() +
            (CONFIRMATION_KEY to AccountManagementConfirmation.DELETE_ROLE.name)

    fun cancelConfirmation(context: Map<String, String>): Map<String, String> = context - CONFIRMATION_KEY

    fun updated(context: Map<String, String>): Map<String, String> =
        context
            .withoutTransientState() +
            (FEEDBACK_KEY to AccountManagementMenuFeedback.UPDATED.name)

    fun invalid(context: Map<String, String>): Map<String, String> =
        context
            .withoutTransientState() +
            (FEEDBACK_KEY to AccountManagementMenuFeedback.INVALID.name)

    fun unavailable(context: Map<String, String>): Map<String, String> =
        context
            .withoutTransientState() +
            (FEEDBACK_KEY to AccountManagementMenuFeedback.UNAVAILABLE.name)

    fun clearFeedback(context: Map<String, String>): Map<String, String> = context - FEEDBACK_KEY

    private fun Map<String, String>.withoutTransientState(): Map<String, String> = this - FEEDBACK_KEY - CONFIRMATION_KEY

    private fun uuidOrNull(value: String?): UUID? = value?.let { raw -> runCatching { UUID.fromString(raw) }.getOrNull() }

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String?): T? =
        value?.let { raw -> enumValues<T>().firstOrNull { enumValue -> enumValue.name == raw } }
}
