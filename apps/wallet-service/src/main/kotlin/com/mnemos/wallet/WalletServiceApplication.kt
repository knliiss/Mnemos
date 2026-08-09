package com.mnemos.wallet

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class WalletServiceApplication

fun main(args: Array<String>) {
    runApplication<WalletServiceApplication>(*args)
}
