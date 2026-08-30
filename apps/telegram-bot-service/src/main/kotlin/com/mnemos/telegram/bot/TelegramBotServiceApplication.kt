package com.mnemos.telegram.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TelegramBotServiceApplication

fun main(args: Array<String>) {
    runApplication<TelegramBotServiceApplication>(*args)
}
