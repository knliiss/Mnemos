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
            "Правило срабатывает, когда совпадают все указанные фильтры." to
                "Правило спрацьовує, коли збігаються всі вказані фільтри.",
            "Только игрок — любой его дроп. Только предмет — этот предмет у любого игрока." to
                "Лише гравець — будь-який його дроп. Лише предмет — цей предмет у будь-якого гравця.",
            "Нажмите номер локации, чтобы включить или выключить её." to
                "Натисніть номер локації, щоб увімкнути або вимкнути її.",
            "Изменения применяются сразу." to "Зміни застосовуються одразу.",
            "Панель управления" to "Панель керування",
            "Выберите раздел" to "Оберіть розділ",
            "SAO и события" to "SAO та події",
            "Глобальные события" to "Глобальні події",
            "Отключить RAID" to "Вимкнути RAID",
            "Включено" to "Увімкнено",
            "Выкл" to "Вимк",
            "Вкл" to "Увімк",
            "Все" to "Усі",
            "Хаос" to "Хаос",
            "Правил дропа пока нет" to "Правил дропу поки немає",
            "Удалить правило?" to "Видалити правило?",
            "Правило уже недоступно." to "Правило вже недоступне.",
            "Это действие нельзя отменить." to "Цю дію не можна скасувати.",
            "Не удалять" to "Не видаляти",
            "Удалить" to "Видалити",
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
            "Правило срабатывает, когда совпадают все указанные фильтры." to
                "The rule triggers when all specified filters match.",
            "Только игрок — любой его дроп. Только предмет — этот предмет у любого игрока." to
                "Player only — any of their drops. Item only — this item for any player.",
            "Нажмите номер локации, чтобы включить или выключить её." to
                "Tap a location number to turn it on or off.",
            "Изменения применяются сразу." to "Changes apply immediately.",
            "Панель управления" to "Control panel",
            "Выберите раздел" to "Choose a section",
            "SAO и события" to "SAO & events",
            "Глобальные события" to "Global events",
            "Отключить RAID" to "Disable RAID",
            "Включено" to "Enabled",
            "Выкл" to "Off",
            "Вкл" to "On",
            "Все" to "All",
            "Хаос" to "Chaos",
            "Правил дропа пока нет" to "There are no drop rules yet",
            "Удалить правило?" to "Delete rule?",
            "Правило уже недоступно." to "This rule is no longer available.",
            "Это действие нельзя отменить." to "This action cannot be undone.",
            "Не удалять" to "Keep rule",
            "Удалить" to "Delete",
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
