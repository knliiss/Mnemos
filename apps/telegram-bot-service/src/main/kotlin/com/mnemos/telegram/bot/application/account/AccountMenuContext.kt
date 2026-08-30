package com.mnemos.telegram.bot.application.account

import com.mnemos.telegram.bot.application.account.model.AccountMenuFeedback
import com.mnemos.telegram.bot.application.account.model.AccountMenuFeedbackType

object AccountMenuContext {
    private const val FEEDBACK_KEY = "account.feedback"

    fun feedback(context: Map<String, String>): AccountMenuFeedback? =
        context[FEEDBACK_KEY]
            ?.let { value -> enumValueOrNull<AccountMenuFeedbackType>(value) }
            ?.let(::AccountMenuFeedback)

    fun switched(): Map<String, String> =
        mapOf(FEEDBACK_KEY to AccountMenuFeedbackType.SWITCHED.name)

    fun unavailable(): Map<String, String> =
        mapOf(FEEDBACK_KEY to AccountMenuFeedbackType.UNAVAILABLE.name)

    fun clear(): Map<String, String> = emptyMap()

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? = enumValues<T>().firstOrNull { it.name == value }
}
