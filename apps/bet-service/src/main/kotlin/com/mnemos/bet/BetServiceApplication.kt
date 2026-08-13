package com.mnemos.bet

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@EnableFeignClients
@SpringBootApplication
class BetServiceApplication

fun main(args: Array<String>) {
    runApplication<BetServiceApplication>(*args)
}
