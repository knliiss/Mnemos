package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage

object BotMenuDesignTranslations {
    fun translate(
        value: String,
        language: TelegramSettingsLanguage,
    ): String {
        val replacements =
            when (language) {
                TelegramSettingsLanguage.RU -> return value
                TelegramSettingsLanguage.UK -> ukrainian
                TelegramSettingsLanguage.EN -> english
            }

        return replacements.fold(value) { translated, replacement ->
            translated.replace(replacement.first, replacement.second)
        }
    }

    private val ukrainian =
        listOf(
            "Панель управления" to "Панель керування",
            "Выберите раздел" to "Оберіть розділ",
            "SAO и события" to "SAO та події",
            "Глобальные события" to "Глобальні події",
            "Включено" to "Увімкнено",
            "Выкл" to "Вимк",
            "Вкл" to "Увімк",
            "Хаос" to "Хаос",
            "Правил дропа пока нет" to "Правил дропу поки немає",
            "Правил" to "Правил",
            "Новое правило дропа" to "Нове правило дропу",
            "Новое правило" to "Нове правило",
            "Создать правило" to "Створити правило",
            "Правило сохранено" to "Правило збережено",
            "Предмет" to "Предмет",
            "Игрок" to "Гравець",
            "Реалмы" to "Реалми",
            "Всего" to "Усього",
            "Активный аккаунт" to "Активний акаунт",
            "Не подключён" to "Не підключено",
            "Не выбран" to "Не вибрано",
            "PRIME и баланс" to "PRIME та баланс",
            "Выбрать" to "Вибрати",
            "Создайте Cristalix-аккаунт или присоединитесь по приглашению." to
                "Створіть Cristalix-акаунт або приєднайтеся за запрошенням.",
            "Не удалось получить настройки уведомлений." to
                "Не вдалося отримати налаштування сповіщень.",
        ).sortedByDescending { replacement -> replacement.first.length }

    private val english =
        listOf(
            "Панель управления" to "Control panel",
            "Выберите раздел" to "Choose a section",
            "SAO и события" to "SAO & events",
            "Глобальные события" to "Global events",
            "Включено" to "Enabled",
            "Выкл" to "Off",
            "Вкл" to "On",
            "Хаос" to "Chaos",
            "Правил дропа пока нет" to "There are no drop rules yet",
            "Правил" to "Rules",
            "Новое правило дропа" to "New drop rule",
            "Новое правило" to "New rule",
            "Создать правило" to "Create rule",
            "Правило сохранено" to "Rule saved",
            "Предмет" to "Item",
            "Игрок" to "Player",
            "Реалмы" to "Realms",
            "Всего" to "Total",
            "Активный аккаунт" to "Active account",
            "Не подключён" to "Not connected",
            "Не выбран" to "Not selected",
            "PRIME и баланс" to "PRIME & balance",
            "Выбрать" to "Select",
            "Создайте Cristalix-аккаунт или присоединитесь по приглашению." to
                "Create a Cristalix account or join by invite.",
            "Не удалось получить настройки уведомлений." to
                "Could not load notification settings.",
        ).sortedByDescending { replacement -> replacement.first.length }
}
