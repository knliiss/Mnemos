package com.mnemos.telegram.bot.application.online

object SaoOnlineMenuContext {
    const val MODE_KEY = "online.mode"
    const val REALM_KEY = "online.realm"
    const val PAGE_KEY = "online.page"
    const val SEARCH_QUERY_KEY = "online.search.query"
    const val SEARCH_ERROR_KEY = "online.search.error"

    const val MODE_REALM = "realm"
    const val MODE_SEARCH = "search"

    fun realm(
        realmNumber: Int,
        page: Int = 0,
    ): Map<String, String> =
        mapOf(
            MODE_KEY to MODE_REALM,
            REALM_KEY to realmNumber.toString(),
            PAGE_KEY to page.toString(),
        )

    fun search(
        query: String,
        page: Int = 0,
    ): Map<String, String> =
        mapOf(
            MODE_KEY to MODE_SEARCH,
            SEARCH_QUERY_KEY to query,
            PAGE_KEY to page.toString(),
        )
}
