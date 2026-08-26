package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.application.personalization.builtin.BuiltinVisualPersonalizationSupport.profileBackground
import com.mnemos.personalization.domain.personalization.model.EffectType
import com.mnemos.wallet.contracts.wallet.shared.Currency

object BuiltinProfileBackgrounds {
    val definitions: List<BuiltinPersonalizationDefinition> =
        listOf(
            background("northern-lights", "Северное сияние", EffectType.AURORA, 58, "#48E5BE", "#6F72FF", 28, 52, 74, 3_400),
            background("acid-fog", "Кислотный туман", EffectType.AURORA, 54, "#CBFF2D", "#4AB55E", 24, 64, 82, 3_100),
            background("red-horizon", "Красный горизонт", EffectType.SHIMMER, 56, "#FF554F", "#FF9B55", 42, 38, 72, 36, Currency.PRIME),
            background("deep-space", "Глубокий космос", EffectType.PARTICLES, 48, "#8A7DFF", "#42C6FF", 22, 78, 48, 2_900),
            background("scanner", "Сканер", EffectType.SHIMMER, 42, "#61E9FF", "#1E7AA8", 72, 34, 54, 2_400),
            background("quiet-pulse", "Тихий пульс", EffectType.PULSE, 38, "#86A7FF", "#293C78", 28, 32, 86, 2_000),
            background("golden-orbit", "Золотая орбита", EffectType.PULSE, 58, "#FFD45F", "#8D6125", 34, 46, 92, 42, Currency.PRIME),
            background("neon-rain", "Неоновый дождь", EffectType.PARTICLES, 62, "#55E7FF", "#FF58DA", 54, 88, 42, 4_200),
            background("ash-sky", "Пепельное небо", EffectType.PARTICLES, 40, "#C7CBC8", "#6A706E", 18, 70, 36, 1_800),
            background("violet-drift", "Фиолетовый дрейф", EffectType.AURORA, 60, "#9A66FF", "#D05CFF", 32, 58, 78, 44, Currency.PRIME),
            background("frozen-stream", "Ледяной поток", EffectType.AURORA, 50, "#B5F5FF", "#4E9DFF", 36, 50, 70, 3_000),
            background("black-signal", "Чёрный сигнал", EffectType.SHIMMER, 36, "#756F90", "#20202A", 64, 28, 62, 1_700),
            background("emerald-network", "Изумрудная сеть", EffectType.PULSE, 54, "#48E092", "#153E2D", 48, 44, 76, 3_300),
            background("singularity", "Сингулярность", EffectType.PULSE, 72, "#B387FF", "#140D24", 24, 56, 100, 58, Currency.PRIME),
            background("solar-dust", "Солнечная пыль", EffectType.PARTICLES, 54, "#FFD05A", "#FF7C3E", 30, 82, 44, 3_500),
            background("crimson-wave", "Багровая волна", EffectType.SHIMMER, 64, "#FF4A62", "#772238", 46, 48, 78, 46, Currency.PRIME),
            background("spectrum", "Спектр", EffectType.AURORA, 64, "#42E8FF", "#F25BFF", 44, 66, 80, 5_100),
            background("cold-vacuum", "Холодный вакуум", EffectType.PARTICLES, 34, "#8EACD6", "#2B3650", 16, 62, 38, 1_600),
            background("emergency-contour", "Аварийный контур", EffectType.SHIMMER, 58, "#FF6A45", "#FFD052", 78, 36, 58, 3_800),
            background("mnemos-core", "Ядро Mnemos", EffectType.AURORA, 68, "#CBFF2D", "#B98DC2", 38, 64, 84, 65, Currency.PRIME),
        )

    private fun background(
        key: String,
        name: String,
        type: EffectType,
        intensity: Int,
        primary: String,
        secondary: String,
        speed: Int,
        density: Int,
        scale: Int,
        amount: Long,
        currency: Currency = Currency.COIN,
    ): BuiltinPersonalizationDefinition =
        profileBackground(
            key = "builtin.profile-background.$key",
            name = name,
            type = type,
            intensity = intensity,
            primary = primary,
            secondary = secondary,
            speed = speed,
            density = density,
            scale = scale,
            currency = currency,
            amount = amount,
        )
}
