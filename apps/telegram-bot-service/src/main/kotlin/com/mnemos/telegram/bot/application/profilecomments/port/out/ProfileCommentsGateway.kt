package com.mnemos.telegram.bot.application.profilecomments.port.out

import com.mnemos.telegram.bot.application.profilecomments.ProfileCommentsPageResult
import java.util.UUID

fun interface ProfileCommentsGateway {
    fun list(accountId: UUID, page: Int): ProfileCommentsPageResult
}
