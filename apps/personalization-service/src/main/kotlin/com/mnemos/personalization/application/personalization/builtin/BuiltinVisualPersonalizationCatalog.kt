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
                colors = listOf(stop("#CBFF2D", 0), stop("#5FE66B", 100)),
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
                colors = listOf(stop("#F0FDFF", 0), stop("#66D9FF", 100)),
                angleDegrees = 0,
                glow = glow("#66D9FF", 45),
                currency = Currency.PRIME,
                amount = 12,
            ),
            nickStyle(
                key = "builtin.nick-style.blood-signal",
                name = "Красный сигнал",
                colors = listOf(stop("#FF3B4F", 0), stop("#FF8A64", 100)),
                angleDegrees = 18,
                glow = glow("#FF3B4F", 32),
                currency = Currency.COIN,
                amount = 1_800,
            ),
            nickStyle(
                key = "builtin.nick-style.ultraviolet",
                name = "Ультрафиолет",
                colors =
                    listOf(
                        stop("#7B5CFF", 0),
                        stop("#D85BFF", 55),
                        stop("#FF8FE8", 100),
                    ),
                angleDegrees = 128,
                glow = glow("#B85CFF", 52),
                currency = Currency.PRIME,
                amount = 22,
            ),
            nickStyle(
                key = "builtin.nick-style.gold-wire",
                name = "Золотая жила",
                colors = listOf(stop("#FFF0A6", 0), stop("#E8B944", 58), stop("#FFF4C2", 100)),
                angleDegrees = 82,
                glow = glow("#E8B944", 28),
                currency = Currency.COIN,
                amount = 3_200,
            ),
            nickStyle(
                key = "builtin.nick-style.monochrome",
                name = "Монохром",
                colors = listOf(stop("#F5F5F2", 0), stop("#8D928B", 100)),
                angleDegrees = 90,
                glow = null,
                currency = Currency.COIN,
                amount = 700,
            ),
            nickStyle(
                key = "builtin.nick-style.cyan-fault",
                name = "Циановый сбой",
                colors = listOf(stop("#55FFF2", 0), stop("#4AA5FF", 46), stop("#CBFF2D", 100)),
                angleDegrees = 145,
                glow = glow("#55FFF2", 46),
                currency = Currency.PRIME,
                amount = 16,
            ),
            profileTheme(
                key = "builtin.profile-theme.terminal",
                name = "Терминал",
                content =
                    theme(
                        background = "#080B07",
                        backgroundSecondary = "#10170B",
                        surface = "#11170F",
                        surfaceSecondary = "#172012",
                        accent = "#CBFF2D",
                        text = "#E8F0DE",
                        border = "#334126",
                        glow = glow("#CBFF2D", 18),
                        angle = 145,
                        texture = 8,
                    ),
                currency = Currency.COIN,
                amount = 2_500,
            ),
            profileTheme(
                key = "builtin.profile-theme.midnight",
                name = "Полночь",
                content =
                    theme(
                        background = "#080A14",
                        backgroundSecondary = "#111326",
                        surface = "#121629",
                        surfaceSecondary = "#191C35",
                        accent = "#7D8CFF",
                        text = "#EEF0FF",
                        border = "#2A3158",
                        glow = glow("#6F7EFF", 22),
                        angle = 155,
                        texture = 5,
                    ),
                currency = Currency.COIN,
                amount = 4_000,
            ),
            profileTheme(
                key = "builtin.profile-theme.ember",
                name = "Тлеющий",
                content =
                    theme(
                        background = "#120B07",
                        backgroundSecondary = "#211108",
                        surface = "#1D110B",
                        surfaceSecondary = "#2A160D",
                        accent = "#FF9C45",
                        text = "#FFF0DF",
                        border = "#5A3020",
                        glow = glow("#FF7A2F", 30),
                        angle = 135,
                        texture = 12,
                    ),
                currency = Currency.PRIME,
                amount = 25,
            ),
            profileTheme(
                key = "builtin.profile-theme.frost",
                name = "Мороз",
                content =
                    theme(
                        background = "#071117",
                        backgroundSecondary = "#0D2631",
                        surface = "#10242C",
                        surfaceSecondary = "#173743",
                        accent = "#86E8FF",
                        text = "#E9FBFF",
                        border = "#27515E",
                        glow = glow("#86E8FF", 24),
                        angle = 165,
                        texture = 4,
                    ),
                currency = Currency.COIN,
                amount = 3_500,
            ),
            profileTheme(
                key = "builtin.profile-theme.violet-noise",
                name = "Пурпурный шум",
                content =
                    theme(
                        background = "#100914",
                        backgroundSecondary = "#21102B",
                        surface = "#1A1121",
                        surfaceSecondary = "#2D1838",
                        accent = "#D569FF",
                        text = "#F7E8FF",
                        border = "#563069",
                        glow = glow("#C95CFF", 34),
                        angle = 118,
                        texture = 18,
                    ),
                currency = Currency.PRIME,
                amount = 40,
            ),
            profileTheme(
                key = "builtin.profile-theme.sandstorm",
                name = "Песчаный фронт",
                content =
                    theme(
                        background = "#161109",
                        backgroundSecondary = "#2A2111",
                        surface = "#211A0F",
                        surfaceSecondary = "#332915",
                        accent = "#E8C16A",
                        text = "#FFF4D8",
                        border = "#66542A",
                        glow = glow("#E8C16A", 16),
                        angle = 132,
                        texture = 22,
                    ),
                currency = Currency.COIN,
                amount = 5_500,
            ),
            profileTheme(
                key = "builtin.profile-theme.red-alert",
                name = "Красная тревога",
                content =
                    theme(
                        background = "#130607",
                        backgroundSecondary = "#26090C",
                        surface = "#1C0A0D",
                        surfaceSecondary = "#351015",
                        accent = "#FF4359",
                        text = "#FFE7EA",
                        border = "#6A2731",
                        glow = glow("#FF4359", 42),
                        angle = 155,
                        texture = 10,
                    ),
                currency = Currency.PRIME,
                amount = 65,
            ),
            profileTheme(
                key = "builtin.profile-theme.oled",
                name = "OLED",
                content =
                    theme(
                        background = "#010201",
                        backgroundSecondary = "#050605",
                        surface = "#080A08",
                        surfaceSecondary = "#0D100D",
                        accent = "#F3F5EF",
                        text = "#F8FAF5",
                        border = "#242824",
                        glow = null,
                        angle = 180,
                        texture = 0,
                    ),
                currency = Currency.COIN,
                amount = 1_800,
            ),
            avatarFrame(
                key = "builtin.avatar-frame.signal",
                name = "Сигнал",
                content = frame("#CBFF2D", "#CBFF2D", 2, 14, 0, FrameStyle.SOLID, glow("#CBFF2D", 20)),
                currency = Currency.COIN,
                amount = 1_300,
            ),
            avatarFrame(
                key = "builtin.avatar-frame.dual",
                name = "Двойной контур",
                content = frame("#6CE5FF", "#A87CFF", 2, 18, 3, FrameStyle.DOUBLE, glow("#7F9CFF", 28)),
                currency = Currency.COIN,
                amount = 2_200,
            ),
            avatarFrame(
                key = "builtin.avatar-frame.markers",
                name = "Маркеры",
                content = frame("#FFD65A", "#CBFF2D", 3, 12, 1, FrameStyle.SEGMENTED, glow("#FFD65A", 25)),
                currency = Currency.PRIME,
                amount = 18,
            ),
            avatarFrame(
                key = "builtin.avatar-frame.frost-glass",
                name = "Ледяной контур",
                content = frame("#9CEBFF", "#E8FCFF", 2, 20, 2, FrameStyle.DOUBLE, glow("#77DAFF", 34)),
                currency = Currency.COIN,
                amount = 1_800,
            ),
            avatarFrame(
                key = "builtin.avatar-frame.ruby",
                name = "Рубин",
                content = frame("#FF465C", "#FF9CA8", 3, 16, 0, FrameStyle.SOLID, glow("#FF465C", 40)),
                currency = Currency.PRIME,
                amount = 14,
            ),
            avatarFrame(
                key = "builtin.avatar-frame.protocol",
                name = "Протокол",
                content = frame("#D8DED2", "#60675E", 2, 8, 2, FrameStyle.SEGMENTED, null),
                currency = Currency.COIN,
                amount = 3_300,
            ),
            avatarFrame(
                key = "builtin.avatar-frame.gold-trace",
                name = "Золотой след",
                content = frame("#F0CA65", "#FFF1AF", 3, 22, 3, FrameStyle.DOUBLE, glow("#E8B944", 30)),
                currency = Currency.PRIME,
                amount = 32,
            ),
            avatarFrame(
                key = "builtin.avatar-frame.void-ring",
                name = "Кольцо пустоты",
                content = frame("#8D6CFF", "#D26CFF", 4, 24, 0, FrameStyle.SOLID, glow("#A67BFF", 48)),
                currency = Currency.COIN,
                amount = 4_200,
            ),
            profileEffect(
                key = "builtin.profile-effect.pulse",
                name = "Импульс",
                content = effect(EffectType.PULSE, 45, "#CBFF2D", "#5FE66B", 50, 45, 50),
                currency = Currency.COIN,
                amount = 3_500,
            ),
            profileEffect(
                key = "builtin.profile-effect.aurora",
                name = "Аврора",
                content = effect(EffectType.AURORA, 62, "#7D8CFF", "#5FE8FF", 35, 55, 70),
                currency = Currency.PRIME,
                amount = 30,
            ),
            profileEffect(
                key = "builtin.profile-effect.sparks",
                name = "Искры",
                content = effect(EffectType.PARTICLES, 55, "#FFD65A", "#FF8B4A", 65, 60, 40),
                currency = Currency.COIN,
                amount = 6_500,
            ),
            profileEffect(
                key = "builtin.profile-effect.shimmer",
                name = "Блик",
                content = effect(EffectType.SHIMMER, 38, "#EEF8FF", "#79DFFF", 72, 30, 85),
                currency = Currency.PRIME,
                amount = 20,
            ),
            profileEffect(
                key = "builtin.profile-effect.ion-storm",
                name = "Ионный шторм",
                content = effect(EffectType.PARTICLES, 78, "#65E6FF", "#956BFF", 82, 82, 55),
                currency = Currency.COIN,
                amount = 8_500,
            ),
            profileEffect(
                key = "builtin.profile-effect.ember-ash",
                name = "Пепел",
                content = effect(EffectType.PARTICLES, 50, "#FF9A45", "#B54A2D", 28, 68, 35),
                currency = Currency.PRIME,
                amount = 24,
            ),
            profileEffect(
                key = "builtin.profile-effect.echo",
                name = "Эхо",
                content = effect(EffectType.PULSE, 66, "#8D6CFF", "#D26CFF", 24, 42, 92),
                currency = Currency.COIN,
                amount = 5_000,
            ),
            profileEffect(
                key = "builtin.profile-effect.cold-dust",
                name = "Холодная пыль",
                content = effect(EffectType.AURORA, 72, "#D9FAFF", "#68CFFF", 18, 76, 110),
                currency = Currency.PRIME,
                amount = 42,
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
        definition(key, PersonalizationType.PROFILE_THEME, name, content, currency, amount)

    private fun avatarFrame(
        key: String,
        name: String,
        content: PersonalizationContent.AvatarFrame,
        currency: Currency,
        amount: Long,
    ): BuiltinPersonalizationDefinition =
        definition(key, PersonalizationType.AVATAR_FRAME, name, content, currency, amount)

    private fun profileEffect(
        key: String,
        name: String,
        content: PersonalizationContent.ProfileEffect,
        currency: Currency,
        amount: Long,
    ): BuiltinPersonalizationDefinition =
        definition(key, PersonalizationType.PROFILE_EFFECT, name, content, currency, amount)

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
            price = PersonalizationPrice(currency = currency, amount = amount),
        )

    private fun theme(
        background: String,
        backgroundSecondary: String,
        surface: String,
        surfaceSecondary: String,
        accent: String,
        text: String,
        border: String,
        glow: GlowStyle?,
        angle: Int,
        texture: Int,
    ): PersonalizationContent.ProfileTheme =
        PersonalizationContent.ProfileTheme(
            background = color(background),
            backgroundSecondary = color(backgroundSecondary),
            surface = color(surface),
            surfaceSecondary = color(surfaceSecondary),
            accent = color(accent),
            text = color(text),
            border = color(border),
            glow = glow,
            gradientAngleDegrees = angle,
            textureStrength = texture,
        )

    private fun frame(
        primary: String,
        secondary: String,
        width: Int,
        radius: Int,
        gap: Int,
        style: FrameStyle,
        glow: GlowStyle?,
    ): PersonalizationContent.AvatarFrame =
        PersonalizationContent.AvatarFrame(
            borderColor = color(primary),
            secondaryBorderColor = color(secondary),
            borderWidthPx = width,
            radiusPx = radius,
            gapPx = gap,
            style = style,
            glow = glow,
        )

    private fun effect(
        type: EffectType,
        intensity: Int,
        primary: String,
        secondary: String?,
        speed: Int,
        density: Int,
        scale: Int,
    ): PersonalizationContent.ProfileEffect =
        PersonalizationContent.ProfileEffect(
            effect = type,
            intensity = intensity,
            primaryColor = color(primary),
            secondaryColor = secondary?.let(::color),
            speed = speed,
            density = density,
            scale = scale,
        )

    private fun stop(
        colorValue: String,
        position: Int,
    ): GradientStop = GradientStop(color = color(colorValue), position = position)

    private fun glow(
        colorValue: String,
        strength: Int,
    ): GlowStyle = GlowStyle(color = color(colorValue), strength = strength)

    private fun color(value: String): HexColor = HexColor(value)
}
