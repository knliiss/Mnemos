package com.mnemos.telegram.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class TelegramBotServiceApplication

fun main(args: Array<String>) {
    runApplication<TelegramBotServiceApplication>(*args)
}
