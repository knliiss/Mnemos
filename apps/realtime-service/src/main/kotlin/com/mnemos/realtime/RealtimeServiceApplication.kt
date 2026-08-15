package com.mnemos.realtime

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class RealtimeServiceApplication

fun main(args: Array<String>) {
    runApplication<RealtimeServiceApplication>(*args)
}
