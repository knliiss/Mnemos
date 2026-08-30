package com.mnemos.telegram.bot.application.daily.port.out

import com.mnemos.telegram.bot.application.daily.model.DailyClaimResult
import com.mnemos.telegram.bot.application.daily.model.DailyRestoreResult
import com.mnemos.telegram.bot.application.daily.model.DailyRewardStateResult

interface DailyRewardGateway {
    fun getState(telegramId: Long): DailyRewardStateResult

    fun claim(telegramId: Long): DailyClaimResult

    fun restore(telegramId: Long): DailyRestoreResult
}
