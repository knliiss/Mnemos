package com.mnemos.social.application.accountreaction.usecase.remove

import java.util.UUID

interface RemoveAccountReactionUseCase {
    fun execute(targetAccountId: UUID)
}
