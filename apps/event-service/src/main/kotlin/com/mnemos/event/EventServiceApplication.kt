package com.mnemos.event

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@EnableFeignClients
@SpringBootApplication
class EventServiceApplication

fun main(args: Array<String>) {
    runApplication<EventServiceApplication>(*args)
}
