package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.application.personalization.builtin.BuiltinVisualPersonalizationSupport.glow
import com.mnemos.personalization.application.personalization.builtin.BuiltinVisualPersonalizationSupport.nickStyle
import com.mnemos.personalization.application.personalization.builtin.BuiltinVisualPersonalizationSupport.stop
import com.mnemos.wallet.contracts.wallet.shared.Currency

object BuiltinNickStylesExpansion {
    val definitions: List<BuiltinPersonalizationDefinition> =
        listOf(
            nickStyle(
                key = "builtin.nick-style.toxic-wave",
                name = "Токсичная волна",
                colors = listOf(stop("#D9FF45", 0), stop("#00F5A0", 48), stop("#28A745", 100)),
                angleDegrees = 32,
                glow = glow("#A8FF35", 44),
                currency = Currency.COIN,
                amount = 2_400,
            ),
            nickStyle(
                key = "builtin.nick-style.coral-shift",
                name = "Коралловый сдвиг",
                colors = listOf(stop("#FF8C7A", 0), stop("#FF5F88", 52), stop("#FFB36B", 100)),
                angleDegrees = 118,
                glow = glow("#FF6F8A", 34),
                currency = Currency.COIN,
                amount = 2_800,
            ),
            nickStyle(
                key = "builtin.nick-style.deep-sea",
                name = "Глубокое море",
                colors = listOf(stop("#78F7FF", 0), stop("#1C8CFF", 50), stop("#2547C8", 100)),
                angleDegrees = 96,
                glow = glow("#48CFFF", 42),
                currency = Currency.PRIME,
                amount = 28,
            ),
            nickStyle(
                key = "builtin.nick-style.white-hot",
                name = "Белое каление",
                colors = listOf(stop("#FFFFFF", 0), stop("#FFE7A8", 44), stop("#FF8D42", 100)),
                angleDegrees = 20,
                glow = glow("#FFF0C0", 54),
                currency = Currency.PRIME,
                amount = 38,
            ),
            nickStyle(
                key = "builtin.nick-style.emerald-cut",
                name = "Изумрудный срез",
                colors = listOf(stop("#C7FFE8", 0), stop("#1FD98C", 48), stop("#0A8256", 100)),
                angleDegrees = 152,
                glow = glow("#2DE59A", 30),
                currency = Currency.COIN,
                amount = 3_600,
            ),
            nickStyle(
                key = "builtin.nick-style.synthwave",
                name = "Синтвейв",
                colors = listOf(stop("#FF4FD8", 0), stop("#9D5CFF", 46), stop("#4EE6FF", 100)),
                angleDegrees = 132,
                glow = glow("#B45CFF", 48),
                currency = Currency.PRIME,
                amount = 45,
            ),
            nickStyle(
                key = "builtin.nick-style.steel-blue",
                name = "Синяя сталь",
                colors = listOf(stop("#E5F2FF", 0), stop("#7798B8", 52), stop("#3E5870", 100)),
                angleDegrees = 74,
                glow = null,
                currency = Currency.COIN,
                amount = 1_600,
            ),
            nickStyle(
                key = "builtin.nick-style.sunset-wire",
                name = "Провод заката",
                colors = listOf(stop("#FFD36D", 0), stop("#FF7A59", 50), stop("#A84CFF", 100)),
                angleDegrees = 108,
                glow = glow("#FF8058", 38),
                currency = Currency.COIN,
                amount = 4_400,
            ),
            nickStyle(
                key = "builtin.nick-style.venom",
                name = "Веном",
                colors = listOf(stop("#F2FF99", 0), stop("#9DFF36", 42), stop("#318C27", 100)),
                angleDegrees = 12,
                glow = glow("#9DFF36", 52),
                currency = Currency.PRIME,
                amount = 34,
            ),
            nickStyle(
                key = "builtin.nick-style.lunar",
                name = "Лунный металл",
                colors = listOf(stop("#FFFFFF", 0), stop("#BFC9E8", 55), stop("#7180A8", 100)),
                angleDegrees = 86,
                glow = glow("#C8D2FF", 24),
                currency = Currency.COIN,
                amount = 2_100,
            ),
            nickStyle(
                key = "builtin.nick-style.magma",
                name = "Магма",
                colors = listOf(stop("#FFF07A", 0), stop("#FF6B2C", 48), stop("#A31919", 100)),
                angleDegrees = 145,
                glow = glow("#FF542D", 56),
                currency = Currency.PRIME,
                amount = 50,
            ),
            nickStyle(
                key = "builtin.nick-style.sakura",
                name = "Сакура",
                colors = listOf(stop("#FFF0F6", 0), stop("#FF9BC7", 52), stop("#D96CBB", 100)),
                angleDegrees = 126,
                glow = glow("#FF9BC7", 28),
                currency = Currency.COIN,
                amount = 3_100,
            ),
        )
}
