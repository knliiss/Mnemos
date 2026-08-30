package com.mnemos.telegram.bot.application.events

import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import java.util.UUID

object EventHistoryMenuContext {
    const val FILTER_KEY = "events.filter"
    const val SEARCH_KEY = "events.search"
    const val SEARCH_ERROR_KEY = "events.search-error"
    const val AFTER_KEY = "events.after"
    const val CURSOR_STACK_KEY = "events.cursor-stack"

    private const val ROOT_CURSOR = "root"
    private const val STACK_SEPARATOR = ","

    fun filter(
        context: Map<String, String>,
        filter: EventHistoryFilter,
    ): Map<String, String> {
        val resetContext = resetPagination(context)

        return if (filter == EventHistoryFilter.ALL) {
            resetContext - FILTER_KEY
        } else {
            resetContext + (FILTER_KEY to filter.name)
        }
    }

    fun selectedFilter(context: Map<String, String>): EventHistoryFilter =
        context[FILTER_KEY]
            ?.let { value -> runCatching { EventHistoryFilter.valueOf(value) }.getOrNull() }
            ?: EventHistoryFilter.ALL

    fun search(
        context: Map<String, String>,
        query: String,
    ): Map<String, String> =
        resetPagination(context) + (SEARCH_KEY to query)

    fun clearSearch(context: Map<String, String>): Map<String, String> =
        resetPagination(context) - SEARCH_KEY

    fun searchQuery(context: Map<String, String>): String? = context[SEARCH_KEY]

    fun searchError(context: Map<String, String>): String? = context[SEARCH_ERROR_KEY]

    fun clearSearchError(context: Map<String, String>): Map<String, String> = context - SEARCH_ERROR_KEY

    fun withSearchError(
        context: Map<String, String>,
        error: String,
    ): Map<String, String> = context + (SEARCH_ERROR_KEY to error)

    fun after(context: Map<String, String>): UUID? =
        context[AFTER_KEY]
            ?.let { value -> runCatching { UUID.fromString(value) }.getOrNull() }

    fun hasPrevious(context: Map<String, String>): Boolean = cursorStack(context).isNotEmpty()

    fun next(
        context: Map<String, String>,
        cursor: UUID,
    ): Map<String, String> {
        val currentCursor = context[AFTER_KEY] ?: ROOT_CURSOR
        val stack = cursorStack(context) + currentCursor

        return context +
            mapOf(
                AFTER_KEY to cursor.toString(),
                CURSOR_STACK_KEY to stack.joinToString(STACK_SEPARATOR),
            )
    }

    fun previous(context: Map<String, String>): Map<String, String> {
        val stack = cursorStack(context)
        val previousCursor = stack.lastOrNull() ?: return context
        val remainingStack = stack.dropLast(1)
        var nextContext = context - AFTER_KEY - CURSOR_STACK_KEY

        if (previousCursor != ROOT_CURSOR) {
            nextContext += AFTER_KEY to previousCursor
        }

        if (remainingStack.isNotEmpty()) {
            nextContext += CURSOR_STACK_KEY to remainingStack.joinToString(STACK_SEPARATOR)
        }

        return nextContext
    }

    private fun resetPagination(context: Map<String, String>): Map<String, String> =
        context - AFTER_KEY - CURSOR_STACK_KEY - SEARCH_ERROR_KEY

    private fun cursorStack(context: Map<String, String>): List<String> =
        context[CURSOR_STACK_KEY]
            ?.split(STACK_SEPARATOR)
            ?.filter(String::isNotBlank)
            .orEmpty()
}
