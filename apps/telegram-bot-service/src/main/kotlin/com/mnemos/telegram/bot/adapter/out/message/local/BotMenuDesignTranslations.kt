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
            "PRIME общий для вас и не меняется при переключении Cristalix-аккаунта." to
                "PRIME спільний для вас і не змінюється під час перемикання Cristalix-акаунта.",
            "Выберите реалм кнопкой ниже или найдите игрока по нику." to
                "Оберіть реалм кнопкою нижче або знайдіть гравця за ніком.",
            "Правило срабатывает, когда совпадают все указанные фильтры." to
                "Правило спрацьовує, коли збігаються всі вказані фільтри.",
            "Только игрок — любой его дроп. Только предмет — этот предмет у любого игрока." to
                "Лише гравець — будь-який його дроп. Лише предмет — цей предмет у будь-якого гравця.",
            "Нажмите номер локации, чтобы включить или выключить её." to
                "Натисніть номер локації, щоб увімкнути або вимкнути її.",
            "Изменения применяются сразу." to "Зміни застосовуються одразу.",
            "Присоединиться по инвайту" to "Приєднатися за інвайтом",
            "Активный Cristalix-аккаунт" to "Активний Cristalix-акаунт",
            "COIN активного аккаунта" to "COIN активного акаунта",
            "PRIME пользователя Mnemos" to "PRIME користувача Mnemos",
            "PRIME пользователя" to "PRIME користувача",
            "Доступные аккаунты" to "Доступні акаунти",
            "Создать аккаунт" to "Створити акаунт",
            "Выберите Cristalix-аккаунт" to "Оберіть Cristalix-акаунт",
            "Поиск игрока онлайн" to "Пошук гравця онлайн",
            "Онлайн и реалмы" to "Онлайн і реалми",
            "Всего онлайн" to "Усього онлайн",
            "Онлайн SAO" to "Онлайн SAO",
            "возврат обрабатывается" to "повернення обробляється",
            "ожидает подтверждения" to "очікує підтвердження",
            "выплата обрабатывается" to "виплата обробляється",
            "выплата не выполнена" to "виплату не виконано",
            "возврат не выполнен" to "повернення не виконано",
            "возврат выполнен" to "повернення виконано",
            "ожидает результата" to "очікує результату",
            "ошибка расчёта" to "помилка розрахунку",
            "Панель управления" to "Панель керування",
            "Выберите раздел" to "Оберіть розділ",
            "SAO и события" to "SAO та події",
            "Глобальные события" to "Глобальні події",
            "Упоминания" to "Згадки",
            "Отключить RAID" to "Вимкнути RAID",
            "Включено" to "Увімкнено",
            "выигрыш" to "виграш",
            "проигрыш" to "програш",
            "Возврат" to "Повернення",
            "Статус" to "Статус",
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
            "PRIME общий для вас и не меняется при переключении Cristalix-аккаунта." to
                "PRIME belongs to your Mnemos user and does not change when switching Cristalix accounts.",
            "Выберите реалм кнопкой ниже или найдите игрока по нику." to
                "Choose a realm below or find a player by nickname.",
            "Правило срабатывает, когда совпадают все указанные фильтры." to
                "The rule triggers when all specified filters match.",
            "Только игрок — любой его дроп. Только предмет — этот предмет у любого игрока." to
                "Player only — any of their drops. Item only — this item for any player.",
            "Нажмите номер локации, чтобы включить или выключить её." to
                "Tap a location number to turn it on or off.",
            "Изменения применяются сразу." to "Changes apply immediately.",
            "Присоединиться по инвайту" to "Join by invite",
            "Активный Cristalix-аккаунт" to "Active Cristalix account",
            "COIN активного аккаунта" to "Active account COIN",
            "PRIME пользователя Mnemos" to "Mnemos user PRIME",
            "PRIME пользователя" to "User PRIME",
            "Доступные аккаунты" to "Available accounts",
            "Создать аккаунт" to "Create account",
            "Выберите Cristalix-аккаунт" to "Select a Cristalix account",
            "Поиск игрока онлайн" to "Find online player",
            "Онлайн и реалмы" to "Online & realms",
            "Всего онлайн" to "Total online",
            "Онлайн SAO" to "SAO online",
            "возврат обрабатывается" to "refund processing",
            "ожидает подтверждения" to "awaiting confirmation",
            "выплата обрабатывается" to "payout processing",
            "выплата не выполнена" to "payout failed",
            "возврат не выполнен" to "refund failed",
            "возврат выполнен" to "refunded",
            "ожидает результата" to "awaiting result",
            "ошибка расчёта" to "settlement failed",
            "Панель управления" to "Control panel",
            "Выберите раздел" to "Choose a section",
            "SAO и события" to "SAO & events",
            "Глобальные события" to "Global events",
            "Упоминания" to "Mentions",
            "Отключить RAID" to "Disable RAID",
            "Включено" to "Enabled",
            "выигрыш" to "won",
            "проигрыш" to "lost",
            "Возврат" to "Refund",
            "Статус" to "Status",
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
