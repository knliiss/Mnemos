package com.mnemos.reward.adapter.inbound.http.internal

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.reward.adapter.inbound.http.internal.TelegramDailyRewardInternalApiMapper.toResponse
import com.mnemos.reward.adapter.inbound.http.internal.TelegramDailyRewardInternalApiMapper.toRestoreResponse
import com.mnemos.reward.application.daily.TelegramDailyRewardService
import com.mnemos.telegram.bot.contracts.internal.daily.TelegramDailyRewardClaimResponse
import com.mnemos.telegram.bot.contracts.internal.daily.TelegramDailyRewardRestoreResponse
import com.mnemos.telegram.bot.contracts.internal.daily.TelegramDailyRewardStateResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/telegram/daily")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class TelegramDailyRewardInternalController(
    private val service: TelegramDailyRewardService,
) {
    @GetMapping("/{telegramId}")
    fun getState(
        @PathVariable telegramId: Long,
    ): ResponseEntity<TelegramDailyRewardStateResponse> {
        val state = service.getState(TelegramId(telegramId)) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(state.toResponse())
    }

    @PostMapping("/{telegramId}/claim")
    fun claim(
        @PathVariable telegramId: Long,
    ): ResponseEntity<TelegramDailyRewardClaimResponse> {
        val reward = service.claim(TelegramId(telegramId)) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(reward.toResponse())
    }

    @PostMapping("/{telegramId}/restore")
    fun restore(
        @PathVariable telegramId: Long,
    ): ResponseEntity<TelegramDailyRewardRestoreResponse> {
        val restoredStreak = service.restore(TelegramId(telegramId)) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(restoredStreak.toRestoreResponse())
    }
}
