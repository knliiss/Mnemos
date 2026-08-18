package com.mnemos.social

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@EnableFeignClients
@SpringBootApplication
class SocialServiceApplication

fun main(args: Array<String>) {
    runApplication<SocialServiceApplication>(*args)
}
