package com.mnemos.bet.adapter.inbound.http.activity

import com.mnemos.bet.adapter.inbound.http.activity.MyActiveBetActivityApiMapper.toResponse
import com.mnemos.bet.application.activity.usecase.GetMyActiveBetActivityService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/bets/me/activity")
class MyActiveBetActivityController(
    private val service: GetMyActiveBetActivityService,
) {
    @GetMapping
    fun get(): MyActiveBetActivityResponse = service.execute().toResponse()
}
