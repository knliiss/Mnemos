package com.mnemos.playerdirectory

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@EnableFeignClients
@SpringBootApplication
class PlayerDirectoryServiceApplication

fun main(args: Array<String>) {
    runApplication<PlayerDirectoryServiceApplication>(*args)
}
