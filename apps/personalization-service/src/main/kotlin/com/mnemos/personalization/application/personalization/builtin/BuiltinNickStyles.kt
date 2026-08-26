package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.application.personalization.builtin.BuiltinVisualPersonalizationSupport.glow
import com.mnemos.personalization.application.personalization.builtin.BuiltinVisualPersonalizationSupport.nickStyle
import com.mnemos.personalization.application.personalization.builtin.BuiltinVisualPersonalizationSupport.stop
import com.mnemos.wallet.contracts.wallet.shared.Currency

object BuiltinNickStyles {
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
            nickStyle(
                key = "builtin.nick-style.blood-signal",
                name = "Красный сигнал",
                colors =
                    listOf(
                        stop("#FF3B4F", 0),
                        stop("#FF8A64", 100),
                    ),
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
                colors =
                    listOf(
                        stop("#FFF0A6", 0),
                        stop("#E8B944", 58),
                        stop("#FFF4C2", 100),
                    ),
                angleDegrees = 82,
                glow = glow("#E8B944", 28),
                currency = Currency.COIN,
                amount = 3_200,
            ),
            nickStyle(
                key = "builtin.nick-style.monochrome",
                name = "Монохром",
                colors =
                    listOf(
                        stop("#F5F5F2", 0),
                        stop("#8D928B", 100),
                    ),
                angleDegrees = 90,
                glow = null,
                currency = Currency.COIN,
                amount = 700,
            ),
            nickStyle(
                key = "builtin.nick-style.cyan-fault",
                name = "Циановый сбой",
                colors =
                    listOf(
                        stop("#55FFF2", 0),
                        stop("#4AA5FF", 46),
                        stop("#CBFF2D", 100),
                    ),
                angleDegrees = 145,
                glow = glow("#55FFF2", 46),
                currency = Currency.PRIME,
                amount = 16,
            ),
        )
}
