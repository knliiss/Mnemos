package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.domain.personalization.model.EffectType
import com.mnemos.personalization.domain.personalization.model.FrameStyle
import com.mnemos.personalization.domain.personalization.model.GlowStyle
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.HexColor
import com.mnemos.personalization.domain.personalization.model.PersonalizationCatalogKey
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.wallet.contracts.wallet.shared.Currency

object BuiltinVisualPersonalizationCatalog {
    val definitions: List<BuiltinPersonalizationDefinition> =
        listOf(
            nickStyle(
                key = "builtin.nick-style.lime-signal",
                name = "Лаймовый сигнал",
                colors =
                    listOf(
                        stop("#CBFF2D", 0),
                        stop("#5FE66B", 100),
                    ),
                angleDegrees = 12,
                glow = glow("#CBFF2D", 35),
                currency = Currency.COIN,
                amount = 900,
            ),
            nickStyle(
                key = "builtin.nick-style.neon-dusk",
                name = "Неоновый закат",
                colors =
                    listOf(
                        stop("#FF7A59", 0),
                        stop("#D26CFF", 52),
                        stop("#6B8CFF", 100),
                    ),
                angleDegrees = 100,
                glow = glow("#D26CFF", 38),
                currency = Currency.COIN,
                amount = 1_400,
            ),
            nickStyle(
                key = "builtin.nick-style.ice-line",
                name = "Ледяная линия",
                colors =
                    listOf(
                        stop("#F0FDFF", 0),
                        stop("#66D9FF", 100),
                    ),
                angleDegrees = 0,
                glow = glow("#66D9FF", 45),
                currency = Currency.PRIME,
                amount = 12,
            ),
            profileTheme(
                key = "builtin.profile-theme.terminal",
                name = "Терминал",
                content =
                    PersonalizationContent.ProfileTheme(
                        background = color("#080B07"),
                        backgroundSecondary = color("#10170B"),
                        surface = color("#11170F"),
                        surfaceSecondary = color("#172012"),
                        accent = color("#CBFF2D"),
                        text = color("#E8F0DE"),
                        border = color("#334126"),
                        glow = glow("#CBFF2D", 18),
                        gradientAngleDegrees = 145,
                        textureStrength = 8,
                    ),
                currency = Currency.COIN,
                amount = 2_500,
            ),
            profileTheme(
                key = "builtin.profile-theme.midnight",
                name = "Полночь",
                content =
                    PersonalizationContent.ProfileTheme(
                        background = color("#080A14"),
                        backgroundSecondary = color("#111326"),
                        surface = color("#121629"),
                        surfaceSecondary = color("#191C35"),
                        accent = color("#7D8CFF"),
                        text = color("#EEF0FF"),
                        border = color("#2A3158"),
                        glow = glow("#6F7EFF", 22),
                        gradientAngleDegrees = 155,
                        textureStrength = 5,
                    ),
                currency = Currency.COIN,
                amount = 4_000,
            ),
            profileTheme(
                key = "builtin.profile-theme.ember",
                name = "Тлеющий",
                content =
                    PersonalizationContent.ProfileTheme(
                        background = color("#120B07"),
                        backgroundSecondary = color("#211108"),
                        surface = color("#1D110B"),
                        surfaceSecondary = color("#2A160D"),
                        accent = color("#FF9C45"),
                        text = color("#FFF0DF"),
                        border = color("#5A3020"),
                        glow = glow("#FF7A2F", 30),
                        gradientAngleDegrees = 135,
                        textureStrength = 12,
                    ),
                currency = Currency.PRIME,
                amount = 25,
            ),
            avatarFrame(
                key = "builtin.avatar-frame.signal",
                name = "Сигнал",
                content =
                    PersonalizationContent.AvatarFrame(
                        borderColor = color("#CBFF2D"),
                        secondaryBorderColor = color("#CBFF2D"),
                        borderWidthPx = 2,
                        radiusPx = 14,
                        gapPx = 0,
                        style = FrameStyle.SOLID,
                        glow = glow("#CBFF2D", 20),
                    ),
                currency = Currency.COIN,
                amount = 1_300,
            ),
            avatarFrame(
                key = "builtin.avatar-frame.dual",
                name = "Двойной контур",
                content =
                    PersonalizationContent.AvatarFrame(
                        borderColor = color("#6CE5FF"),
                        secondaryBorderColor = color("#A87CFF"),
                        borderWidthPx = 2,
                        radiusPx = 18,
                        gapPx = 3,
                        style = FrameStyle.DOUBLE,
                        glow = glow("#7F9CFF", 28),
                    ),
                currency = Currency.COIN,
                amount = 2_200,
            ),
            avatarFrame(
                key = "builtin.avatar-frame.markers",
                name = "Маркеры",
                content =
                    PersonalizationContent.AvatarFrame(
                        borderColor = color("#FFD65A"),
                        secondaryBorderColor = color("#CBFF2D"),
                        borderWidthPx = 3,
                        radiusPx = 12,
                        gapPx = 1,
                        style = FrameStyle.SEGMENTED,
                        glow = glow("#FFD65A", 25),
                    ),
                currency = Currency.PRIME,
                amount = 18,
            ),
            profileEffect(
                key = "builtin.profile-effect.pulse",
                name = "Импульс",
                content =
                    PersonalizationContent.ProfileEffect(
                        effect = EffectType.PULSE,
                        intensity = 45,
                        primaryColor = color("#CBFF2D"),
                        secondaryColor = color("#5FE66B"),
                        speed = 50,
                        density = 45,
                        scale = 50,
                    ),
                currency = Currency.COIN,
                amount = 3_500,
            ),
            profileEffect(
                key = "builtin.profile-effect.aurora",
                name = "Аврора",
                content =
                    PersonalizationContent.ProfileEffect(
                        effect = EffectType.AURORA,
                        intensity = 62,
                        primaryColor = color("#7D8CFF"),
                        secondaryColor = color("#5FE8FF"),
                        speed = 35,
                        density = 55,
                        scale = 70,
                    ),
                currency = Currency.PRIME,
                amount = 30,
            ),
            profileEffect(
                key = "builtin.profile-effect.sparks",
                name = "Искры",
                content =
                    PersonalizationContent.ProfileEffect(
                        effect = EffectType.PARTICLES,
                        intensity = 55,
                        primaryColor = color("#FFD65A"),
                        secondaryColor = color("#FF8B4A"),
                        speed = 65,
                        density = 60,
                        scale = 40,
                    ),
                currency = Currency.COIN,
                amount = 6_500,
            ),
        )

    private fun nickStyle(
        key: String,
        name: String,
        colors: List<GradientStop>,
        angleDegrees: Int,
        glow: GlowStyle?,
        currency: Currency,
        amount: Long,
    ): BuiltinPersonalizationDefinition =
        definition(
            key = key,
            type = PersonalizationType.NICK_STYLE,
            name = name,
            content =
                PersonalizationContent.NickStyle(
                    colors = colors,
                    angleDegrees = angleDegrees,
                    glow = glow,
                ),
            currency = currency,
            amount = amount,
        )

    private fun profileTheme(
        key: String,
        name: String,
        content: PersonalizationContent.ProfileTheme,
        currency: Currency,
        amount: Long,
    ): BuiltinPersonalizationDefinition =
        definition(
            key = key,
            type = PersonalizationType.PROFILE_THEME,
            name = name,
            content = content,
            currency = currency,
            amount = amount,
        )

    private fun avatarFrame(
        key: String,
        name: String,
        content: PersonalizationContent.AvatarFrame,
        currency: Currency,
        amount: Long,
    ): BuiltinPersonalizationDefinition =
        definition(
            key = key,
            type = PersonalizationType.AVATAR_FRAME,
            name = name,
            content = content,
            currency = currency,
            amount = amount,
        )

    private fun profileEffect(
        key: String,
        name: String,
        content: PersonalizationContent.ProfileEffect,
        currency: Currency,
        amount: Long,
    ): BuiltinPersonalizationDefinition =
        definition(
            key = key,
            type = PersonalizationType.PROFILE_EFFECT,
            name = name,
            content = content,
            currency = currency,
            amount = amount,
        )

    private fun definition(
        key: String,
        type: PersonalizationType,
        name: String,
        content: PersonalizationContent,
        currency: Currency,
        amount: Long,
    ): BuiltinPersonalizationDefinition =
        BuiltinPersonalizationDefinition(
            catalogKey = PersonalizationCatalogKey(key),
            type = type,
            name = name,
            content = content,
            price =
                PersonalizationPrice(
                    currency = currency,
                    amount = amount,
                ),
        )

    private fun stop(
        color: String,
        position: Int,
    ): GradientStop =
        GradientStop(
            color = color(color),
            position = position,
        )

    private fun glow(
        color: String,
        strength: Int,
    ): GlowStyle =
        GlowStyle(
            color = color(color),
            strength = strength,
        )

    private fun color(value: String): HexColor = HexColor(value)
}
