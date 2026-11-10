package com.mnemos.telegram.bot.application.profile.port.out

import com.mnemos.telegram.bot.application.profile.model.PublicProfileDetailResult
import com.mnemos.telegram.bot.application.profile.model.PublicProfileSearchResult
import java.util.UUID

interface PublicProfileGateway {
    fun search(query: String, page: Int, size: Int): PublicProfileSearchResult

    fun get(accountId: UUID): PublicProfileDetailResult
}
