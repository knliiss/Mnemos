package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage

object AccountMenuTranslations {
    fun translate(
        value: String,
        language: TelegramSettingsLanguage,
    ): String {
        val replacements =
            when (language) {
                TelegramSettingsLanguage.UK -> ukrainian
                TelegramSettingsLanguage.EN -> english
                TelegramSettingsLanguage.RU -> return value
            }

        return replacements.fold(value) { translated, replacement ->
            translated.replace(replacement.first, replacement.second)
        }
    }

    private val ukrainian =
        listOf(
            "Этот Cristalix-аккаунт уже зарегистрирован в Mnemos и не может быть создан повторно. Если это ваш аккаунт, присоединитесь к нему по приглашению." to
                "Цей Cristalix-акаунт уже зареєстрований у Mnemos і не може бути створений повторно. Якщо це ваш акаунт, приєднайтеся до нього за запрошенням.",
            "Ник Cristalix имеет неверный формат. Отправьте ник из 4–20 букв, цифр или <code>_</code>." to
                "Нік Cristalix має неправильний формат. Надішліть нік із 4–20 літер, цифр або <code>_</code>.",
            "Игрок Cristalix с таким ником не найден. Проверьте написание и отправьте ник ещё раз." to
                "Гравця Cristalix із таким ніком не знайдено. Перевірте написання та надішліть нік ще раз.",
            "Код приглашения недействителен или больше недоступен. Отправьте другой код." to
                "Код запрошення недійсний або більше недоступний. Надішліть інший код.",
            "Сервис временно недоступен. Ввод сохранён — можно повторить сообщение позже." to
                "Сервіс тимчасово недоступний. Введення збережено — повідомлення можна повторити пізніше.",
            "Создайте Cristalix-аккаунт или присоединитесь по приглашению." to
                "Створіть Cristalix-акаунт або приєднайтеся за запрошенням.",
            "Отправьте <b>ник Cristalix</b> обычным сообщением." to
                "Надішліть <b>нік Cristalix</b> звичайним повідомленням.",
            "Отправьте <b>код приглашения</b> обычным сообщением." to
                "Надішліть <b>код запрошення</b> звичайним повідомленням.",
        ).sortedByDescending { replacement -> replacement.first.length }

    private val english =
        listOf(
            "Этот Cristalix-аккаунт уже зарегистрирован в Mnemos и не может быть создан повторно. Если это ваш аккаунт, присоединитесь к нему по приглашению." to
                "This Cristalix account is already registered in Mnemos and cannot be created again. If it is your account, join it using an invite.",
            "Ник Cristalix имеет неверный формат. Отправьте ник из 4–20 букв, цифр или <code>_</code>." to
                "The Cristalix nickname has an invalid format. Send a nickname containing 4–20 letters, digits, or <code>_</code>.",
            "Игрок Cristalix с таким ником не найден. Проверьте написание и отправьте ник ещё раз." to
                "No Cristalix player with that nickname was found. Check the spelling and send the nickname again.",
            "Код приглашения недействителен или больше недоступен. Отправьте другой код." to
                "The invite code is invalid or no longer available. Send another code.",
            "Сервис временно недоступен. Ввод сохранён — можно повторить сообщение позже." to
                "The service is temporarily unavailable. Your input was kept, so you can send the message again later.",
            "Создайте Cristalix-аккаунт или присоединитесь по приглашению." to
                "Create a Cristalix account or join one using an invite.",
            "Отправьте <b>ник Cristalix</b> обычным сообщением." to
                "Send your <b>Cristalix nickname</b> as a regular message.",
            "Отправьте <b>код приглашения</b> обычным сообщением." to
                "Send the <b>invite code</b> as a regular message.",
        ).sortedByDescending { replacement -> replacement.first.length }
}
