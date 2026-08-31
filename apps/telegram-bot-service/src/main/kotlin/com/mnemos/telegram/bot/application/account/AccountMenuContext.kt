package com.mnemos.telegram.bot.application.account

import com.mnemos.telegram.bot.application.account.model.AccountMenuFeedback
import com.mnemos.telegram.bot.application.account.model.AccountMenuFeedbackType

object AccountMenuContext {
    private const val FEEDBACK_KEY = "account.feedback"

    fun feedback(context: Map<String, String>): AccountMenuFeedback? =
        context[FEEDBACK_KEY]
            ?.let { value -> enumValueOrNull<AccountMenuFeedbackType>(value) }
            ?.let(::AccountMenuFeedback)

    fun switched(): Map<String, String> = feedback(AccountMenuFeedbackType.SWITCHED)

    fun created(): Map<String, String> = feedback(AccountMenuFeedbackType.CREATED)

    fun joined(): Map<String, String> = feedback(AccountMenuFeedbackType.JOINED)

    fun createInvalid(): Map<String, String> = feedback(AccountMenuFeedbackType.CREATE_INVALID)

    fun joinInvalid(): Map<String, String> = feedback(AccountMenuFeedbackType.JOIN_INVALID)

    fun unavailable(): Map<String, String> = feedback(AccountMenuFeedbackType.UNAVAILABLE)

    fun clear(): Map<String, String> = emptyMap()

    private fun feedback(type: AccountMenuFeedbackType): Map<String, String> = mapOf(FEEDBACK_KEY to type.name)

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? =
        enumValues<T>().firstOrNull { enumValue -> enumValue.name == value }
}
