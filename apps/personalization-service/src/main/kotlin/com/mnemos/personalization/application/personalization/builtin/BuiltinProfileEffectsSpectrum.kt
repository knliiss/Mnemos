package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.application.personalization.builtin.BuiltinVisualPersonalizationSupport.profileEffect
import com.mnemos.personalization.domain.personalization.model.EffectType
import com.mnemos.wallet.contracts.wallet.shared.Currency

object BuiltinProfileEffectsSpectrum {
    val definitions: List<BuiltinPersonalizationDefinition> =
        listOf(
            effect("lime-dust", "Лаймовая пыль", EffectType.PARTICLES, 52, "#CBFF2D", "#75FF8A", 44, 68, 48, 2_600),
            effect("red-shimmer", "Красный блик", EffectType.SHIMMER, 58, "#FF5A5A", "#FFB06A", 62, 42, 58, 3_100),
            effect("violet-aurora", "Фиолетовая аврора", EffectType.AURORA, 64, "#9C6BFF", "#FF65D6", 34, 55, 67, 42, Currency.PRIME),
            effect("cyan-pulse", "Циановый импульс", EffectType.PULSE, 48, "#55DFFF", "#A8F3FF", 58, 38, 62, 2_900),
            effect("gold-particles", "Золотая пыль", EffectType.PARTICLES, 56, "#FFD35A", "#FFF1A8", 38, 72, 44, 36, Currency.PRIME),
            effect("green-shimmer", "Зелёный скан", EffectType.SHIMMER, 44, "#56FF92", "#CBFF2D", 68, 35, 52, 2_300),
            effect("cold-aurora", "Холодная аврора", EffectType.AURORA, 50, "#75D9FF", "#657CFF", 28, 52, 72, 3_400),
            effect("ember-pulse", "Угольный пульс", EffectType.PULSE, 62, "#FF6738", "#FFB23D", 46, 44, 66, 48, Currency.PRIME),
            effect("rose-dust", "Розовая пыль", EffectType.PARTICLES, 48, "#FF8FC6", "#E96CFF", 32, 76, 42, 2_800),
            effect("white-shimmer", "Белый блик", EffectType.SHIMMER, 38, "#FFFFFF", "#BFE6FF", 56, 28, 60, 2_100),
            effect("void-pulse", "Импульс пустоты", EffectType.PULSE, 68, "#7154D9", "#1A122D", 36, 48, 78, 52, Currency.PRIME),
            effect("spectrum-aurora", "Спектральная аврора", EffectType.AURORA, 58, "#49E5FF", "#F05CFF", 42, 62, 70, 4_600),
        )

    private fun effect(
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
        profileEffect(
            key = "builtin.profile-effect.$key",
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
