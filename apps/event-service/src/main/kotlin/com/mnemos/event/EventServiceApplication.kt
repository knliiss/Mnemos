package com.mnemos.event

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EventServiceApplication

fun main(args: Array<String>) {
    runApplication<EventServiceApplication>(*args)
}
