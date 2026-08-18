package com.mnemos.social.adapter.out.persistence.reaction

import java.io.Serializable
import java.util.UUID

data class AccountReactionEntityId(
    var actorAccountId: UUID? = null,
    var targetAccountId: UUID? = null,
) : Serializable
