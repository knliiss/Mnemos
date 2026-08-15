package com.mnemos.personalization.domain.personalization.model

enum class PersonalizationType(
    val target: PersonalizationTarget,
) {
    NOTIFICATION_PACK(PersonalizationTarget.USER),
    NICK_STYLE(PersonalizationTarget.ACCOUNT),
    PROFILE_THEME(PersonalizationTarget.ACCOUNT),
    AVATAR_FRAME(PersonalizationTarget.ACCOUNT),
    PROFILE_EFFECT(PersonalizationTarget.ACCOUNT),
}
